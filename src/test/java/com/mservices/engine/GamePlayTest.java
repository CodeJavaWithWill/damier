package com.mservices.engine;

import com.mservices.board.Board;
import com.mservices.board.Tile;
import com.mservices.piece.Piece;
import com.mservices.piece.PieceColor;
import com.mservices.piece.PieceType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Damier Core Gameplay Logic Tests")
class GamePlayTest {

    private Board board;
    private MoveEngine moveEngine;

    @BeforeEach
    void setUp() {
        // Initialize an empty board configuration for customized structural testing
        board = new Board(BoardInitialization.EMPTY);
        moveEngine = new MoveEngine(board);
    }

    @Nested
    @DisplayName("Standard Movement Constraints")
    class StandardMovementTests {

        @Test
        @DisplayName("Man piece should move diagonally forward one step into empty tile")
        void manShouldMoveDiagonallyForward() {
            // Arrange: Place a White Man at row 6, col 1 (Dark Tile)
            Piece whiteMan = new Piece(PieceColor.WHITE, PieceType.MAN);
            Tile startTile = board.getTile(6, 1);
            startTile.setPiece(whiteMan);

            Tile targetTile = board.getTile(5, 2);

            // Act
            boolean isMoveExecuted = moveEngine.tryExecuteMove(startTile, targetTile);

            // Assert
            assertAll(
                    () -> assertTrue(isMoveExecuted, "Valid diagonal forward move failed to execute"),
                    () -> assertTrue(startTile.isEmpty(), "Source tile was not cleared after moving"),
                    () -> assertEquals(whiteMan, targetTile.getPiece(), "Piece was not deposited on target tile")
            );
        }

        @Test
        @DisplayName("Man piece must not move backward during standard movement phases")
        void manMustNotMoveBackward() {
            // Arrange: White Man at 5, 2
            Tile startTile = board.getTile(5, 2);
            startTile.setPiece(new Piece(PieceColor.WHITE, PieceType.MAN));

            // Backward target for white (row increase)
            Tile invalidTargetTile = board.getTile(6, 1);

            // Act
            boolean isMoveExecuted = moveEngine.tryExecuteMove(startTile, invalidTargetTile);

            // Assert
            assertFalse(isMoveExecuted, "Rule violation: Man piece allowed to walk backwards");
        }
    }

    @Nested
    @DisplayName("Capture & Jump Engine Protocol")
    class CaptureEngineTests {

        @Test
        @DisplayName("Man piece can capture an opponent piece backwards and forwards")
        void manShouldCaptureEnemyPieceBackwardsAndForwards() {
            // Arrange: White Man at 5,2 surrounded by two black pieces
            Piece whiteMan = new Piece(PieceColor.WHITE, PieceType.MAN);
            board.getTile(5, 2).setPiece(whiteMan);

            // Forward Enemy at 4,3 with landing zone at 3,4
            board.getTile(4, 3).setPiece(new Piece(PieceColor.BLACK, PieceType.MAN));
            // Backward Enemy at 6,3 with landing zone at 7,4
            board.getTile(6, 3).setPiece(new Piece(PieceColor.BLACK, PieceType.MAN));

            Tile forwardTarget = board.getTile(3, 4);
            Tile backwardTarget = board.getTile(7, 4);

            // Act & Assert (Verify both directions pass check metrics)
            assertTrue(moveEngine.isValidCapturePath(board.getTile(5, 2), forwardTarget), "Forward jump invalid");
            assertTrue(moveEngine.isValidCapturePath(board.getTile(5, 2), backwardTarget), "Backward jump invalid");
        }

        @Test
        @DisplayName("Forced Capture: Engine must block normal moves when capture paths exist")
        void shouldEnforceForcedCaptureConstraint() {
            // Arrange: White Man can capture an enemy or do a peaceful move
            board.getTile(6, 1).setPiece(new Piece(PieceColor.WHITE, PieceType.MAN));
            board.getTile(5, 2).setPiece(new Piece(PieceColor.BLACK, PieceType.MAN)); // Enemy target

            Tile peacefulTarget = board.getTile(5, 0); // Open valid square

            // Act
            List<Move> legalMoves = moveEngine.calculateLegalMoves(PieceColor.WHITE);

            // Assert using coordinates to avoid identity-coupled assertions
            boolean containsPeacefulMove = legalMoves.stream()
                    .anyMatch(m -> m.getDestination().getRow() == peacefulTarget.getRow()
                            && m.getDestination().getCol() == peacefulTarget.getCol());

            assertFalse(containsPeacefulMove, "Rule violation: Engine allowed regular move when capture exists");
        }

        @Test
        @DisplayName("Majority Rule: Force the selection of the longest capture route string")
        void shouldEnforceMaximumQuantityCaptureRule() {
            // Arrange: Set up path options where path A takes 1 piece, path B takes 2 pieces
            board.getTile(8, 1).setPiece(new Piece(PieceColor.WHITE, PieceType.MAN)); // Attacker

            // Path A targets (1 capture)
            board.getTile(7, 2).setPiece(new Piece(PieceColor.BLACK, PieceType.MAN));

            // Path B targets (2 captures chain)
            board.getTile(7, 0).setPiece(new Piece(PieceColor.BLACK, PieceType.MAN));
            board.getTile(5, 2).setPiece(new Piece(PieceColor.BLACK, PieceType.MAN));

            // Act
            List<MoveChain> optimalChains = moveEngine.calculateOptimalCaptureChains(PieceColor.WHITE);

            // Assert
            assertFalse(optimalChains.isEmpty(), "No optimal tracks found");
            assertEquals(2, optimalChains.get(0).getCaptureCount(), "Engine failed to prioritize the route containing 2 pieces");
        }
    }

    @Nested
    @DisplayName("Promotion Logic Sequences")
    class PromotionTests {

        @Test
        @DisplayName("Man promotes to King only when completing its turn on opponent baseline")
        void manShouldPromoteToKingOnOpponentBaseline() {
            // Arrange: White Man 1 step away from baseline (Row 0 for White)
            Tile finalStepBeforePromotion = board.getTile(1, 2);
            Piece promotingPiece = new Piece(PieceColor.WHITE, PieceType.MAN);
            finalStepBeforePromotion.setPiece(promotingPiece);

            Tile baselineTile = board.getTile(0, 3);

            // Act
            moveEngine.tryExecuteMove(finalStepBeforePromotion, baselineTile);

            // Assert
            assertAll(
                    () -> assertEquals(PieceType.KING, baselineTile.getPiece().getType(), "Man was not promoted to King upon reaching the baseline"),
                    () -> assertTrue(finalStepBeforePromotion.isEmpty(), "Source tile was not safely swept clear")
            );
        }

        @Test
        @DisplayName("Turkish Protocol: Man passing through baseline during multi-jump chain does NOT promote")
        void manPassingThroughBaselineShouldNotPromote() {
            // Arrange: White piece jumps onto row 0, but must jump right back out to row 2 to complete capture chain
            Tile startTile = board.getTile(2, 1);
            Piece jumpingMan = new Piece(PieceColor.WHITE, PieceType.MAN);
            startTile.setPiece(jumpingMan);

            board.getTile(1, 2).setPiece(new Piece(PieceColor.BLACK, PieceType.MAN)); // Enemy 1
            board.getTile(1, 4).setPiece(new Piece(PieceColor.BLACK, PieceType.MAN)); // Enemy 2

            // Steps: (2,1) -> jumps over (1,2) -> lands on Baseline (0,3) -> jumps over (1,4) -> lands on (2,5)
            Tile baselineMidwayPoint = board.getTile(0, 3);
            Tile finalLandingPoint = board.getTile(2, 5);

            // Act: Execute the complete sequential turn chain
            moveEngine.executeMultiJumpSequence(startTile, List.of(baselineMidwayPoint, finalLandingPoint));

            // Assert
            assertAll(
                    () -> assertEquals(PieceType.MAN, finalLandingPoint.getPiece().getType(), "Rule violation: Man promoted despite having lingering captures to fulfill"),
                    () -> assertTrue(baselineMidwayPoint.isEmpty(), "Midway landing vector was not cleared")
            );
        }
    }
}
