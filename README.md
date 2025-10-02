# Tak - A Strategic Board Game

Welcome to **Tak**, a digital rendition of the classic strategy board game. This implementation brings the timeless tactics and strategy of Tak to your computer, allowing you to play against another human player locally or against intelligent AI agents.

---

## Introduction

**Tak** is a two-player abstract strategy game that challenges players to build a road connecting opposite sides of the board. It combines simple rules with deep strategy, making it enjoyable for both casual players and serious tacticians.

This project is built using **LibGDX**, a cross-platform Java game development framework, providing smooth graphics and responsive controls.

---

## Game Rules

### Objective

- **Road Victory**: Be the first to create a continuous path (road) of your pieces connecting opposite sides of the board.
- **Flat Victory**: If the board is completely filled with no road victory, the player with the most flat stones on top of stacks wins.

### Components

- **Flat Stones**: Basic pieces that can be part of a road and can be stacked.
- **Standing Stones (Walls)**: Block opponent's road but cannot be part of your own road.
- **Capstones**: Special pieces that can flatten standing stones and can be part of a road.

### Basic Rules

#### Setup
- The game is played on a square board of size 5x5.
- Each player starts with 21 flat stones, 10 standing stones, and 1 capstone.

#### First Move
- Players take turns placing one of their opponent's flat stones on the board.
- No standing stones or capstones can be placed during the first two moves.

#### Turns
- On your turn, you may either place a piece or move a stack.

#### Placing a Piece
- Place a flat stone, standing stone, or capstone on an empty square.
- Pieces are placed on your own side starting from the third move.

#### Moving a Stack
- You may move pieces from a stack you control (your piece is on top).
- Move pieces in a straight line (up, down, left, or right).
- You can move up to as many pieces as the board size (maximum carry limit).
- You must drop at least one piece on each square you move over.

#### Stacking Rules
- You can stack your pieces on top of flat stones.
- Standing stones cannot have pieces stacked on top of them unless by a capstone.
- Capstones cannot have any pieces stacked on top of them.

#### Capstone Movement
- Capstones can flatten standing stones (turn them into flat stones) when moved onto them.
- Capstones count as part of your road.

#### Winning the Game
- **Road Victory**: Achieved immediately when a player connects opposite sides with a continuous road.
- **Flat Victory**: If the board is filled and no road victory is declared, the player with the most flat stones on top wins.

---

## How to Play

### Controls

#### Selecting a Piece
- Use the **hotbar** in the middle of the screen to select a piece type (flat stone, standing stone, or capstone).
- The available pieces are shown along with the remaining count.

#### Placing a Piece
- Click on an empty square on the board to place your selected piece.

#### Moving a Stack
- Click on a stack where your piece is on top to select it.
- The selected stack will be highlighted.
- Click on a square in the direction you want to move (must be adjacent in a straight line).
- If moving more than one piece, you will be prompted to enter drop counts separated by commas.  
  Example: `1,1` to drop one piece on the first square and one on the next.

---

### Notes
- **First Two Moves**: Only flat stones can be placed, and they must be your opponent's stones.
- **Piece Limits**: Keep an eye on the remaining pieces displayed next to the hotbar.
- **Current Player**: The current player is indicated on the top right of the screen.

---

## Playing Against AI

- **Minimax Heuristic Agent**: Uses classical Minimax search with heuristics.
- **ANN Agent (Artificial Neural Network)**: Evaluates moves using a trained neural network.
- **Combined Minimax + ANN Agent**: Uses Minimax search with neural network evaluation for stronger AI decision-making.

You can also **train new neural networks** using the provided trainers to improve the ANN agent’s performance.

---

## Installation and Running the Game

### Prerequisites

- **Java Development Kit (JDK 8)** or higher.
- **Gradle** (included via Gradle wrapper in the project).

### Steps

#### Clone the Repository
```bash
git clone https://github.com/katarinadvornak/TakGame.git
cd TakGame
