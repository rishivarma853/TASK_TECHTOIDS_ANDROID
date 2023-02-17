package ca.lcit22fw.madt.techtoids.android.nota_app.Models;

import static android.content.ContentValues.TAG;

import android.util.Log;

import java.util.ArrayList;
import java.util.Objects;

public class BoardList {

    ArrayList<Board> boardList = new ArrayList<>();

    public BoardList() {

    }

    public BoardList(ArrayList<Board> boardList) {
        this.boardList = boardList;
    }

    public ArrayList<Board> getBoardList() {
        return boardList;
    }

    public void setBoardList(ArrayList<Board> boardList) {
        this.boardList = boardList;
    }

    public void addBoardToList(Board board) {
        this.boardList.add(board);
    }

    public void updateBoard(Board oldBoard, Board newboard) {
        if(boardList.contains(oldBoard)) {
            boardList.set(boardList.indexOf(oldBoard), newboard);
        } else {
            Log.e(TAG, "board Not found");
        }
//        boolean found = false;
//        int position = 0;
//        for(int i = 0; i < boardList.size(); i++) {
//            Board brd = boardList.get(i);
//            if(brd.getBoardId().equals(oldBoardUID)) {
//                found = true;
//                position = i;
//                break;
//            }
//        }
//        if(found) {
//            boardList.set(position, board);
//        }
    }
    public void deleteBoard(Board board) {
        boardList.remove(board);
    }

    public Board getBoard(String boardUID) {
        for(Board board : boardList) {
            if(Objects.equals(board.getBoardId(), boardUID)){
                return board;
            }
        }
        return null;
    }
    public Board getBoard(int position) {
        return boardList.get(position);
    }

}
