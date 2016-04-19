# Battleship --- Project by Jessica B and Robert I
Simple Battleship game using server/client set up and GUI

# Overall steps of a game
  1. Server collects two players and starts game
  2. Player 1 selects first move coords
  3. Server recieves and sends selected cords to player 2
  4. player 2 checks if it's a hit with boolean, returns
    + updates it's bottom grid
    + adds to player1Count
  5. server pushes boolean to player 1
  6. player 1 updates topgrid
  7. repeat for player 2
  8. continue until player1 or player2 count hits 12
    +number of hits to destroy all ships


# Methods List
# Known Problems & To Do
+ stopping player from moving before turn
  - appears to be "queueing" moves, but still needs click to send first of queue
+ no winner display
+ no disconnect
