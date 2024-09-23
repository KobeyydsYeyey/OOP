package com.example;

import java.io.*;
import java.util.Arrays;
import java.util.Base64;
import java.util.HashSet;
import java.util.Set;

class Grid implements Serializable{
    private static final long serialVersionUID = 1L;
    public static final int BOX_SIZE = 3;
    public static final int GRID_SIZE = 9;
    private int[][] grid;

    public Grid() {
        this.grid = new int[GRID_SIZE][GRID_SIZE];
    }

    public Grid(int[][] grid) {
        this.grid = grid;
    }

    public int[] getRow(int row) {
        return grid[row];
    }

    public int[] getColumn(int col) {
        return Arrays.stream(grid).mapToInt(r -> r[col]).toArray();
    }

    public int[][] getBox(int row, int col) {
        int[][] box = new int[BOX_SIZE][BOX_SIZE];
        int startRow = (row / BOX_SIZE) * BOX_SIZE;
        int startCol = (col / BOX_SIZE) * BOX_SIZE;
        for (int i = 0; i < BOX_SIZE; i++) {
            box[i] = Arrays.copyOfRange(grid[startRow + i], startCol, startCol + BOX_SIZE);
        }
        return box;
    }

    public void setCell(int row, int col, int value) {
        grid[row][col] = value;
    }

    public int getCell(int row, int col) {
        return grid[row][col];
    }

    public void printGrid() {
        for (int[] row : grid) {
            System.out.println(Arrays.toString(row));
        }
    }

    public Grid clone() {
        return new Grid(Arrays.stream(grid).map(int[]::clone).toArray(int[][]::new));
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof Grid)) return false;
        Grid other = (Grid) obj;
        return Arrays.deepEquals(this.grid, other.grid);
    }
}

class Sudoku implements Serializable {
    private static final long serialVersionUID = 1L;
    private Grid grid;

    public Sudoku(Grid grid) {
        this.grid = grid;
    }

    public static Sudoku parse(String inputStr) {
        int[][] gridData = new int[Grid.GRID_SIZE][Grid.GRID_SIZE];
        for (int i = 0; i < inputStr.length(); i++) {
            int row = i / Grid.GRID_SIZE;
            int col = i % Grid.GRID_SIZE;
            gridData[row][col] = Character.getNumericValue(inputStr.charAt(i));
        }
        return new Sudoku(new Grid(gridData));
    }

    public Set<Integer>[][] getInference() {
        Set<Integer>[][] candidates = new HashSet[Grid.GRID_SIZE][Grid.GRID_SIZE];
        for (int row = 0; row < Grid.GRID_SIZE; row++) {
            for (int col = 0; col < Grid.GRID_SIZE; col++) {
                candidates[row][col] = new HashSet<>();
                if (grid.getCell(row, col) == 0) {
                    candidates[row][col] = getPossibleValues(row, col);
                }
            }
        }
        return candidates;
    }

    private Set<Integer> getPossibleValues(int row, int col) {
        Set<Integer> possibleValues = new HashSet<>(Arrays.asList(1, 2, 3, 4, 5, 6, 7, 8, 9));
        for (int value : grid.getRow(row)) {
            possibleValues.remove(value);
        }
        for (int value : grid.getColumn(col)) {
            possibleValues.remove(value);
        }
        for (int[] boxRow : grid.getBox(row, col)) {
            for (int value : boxRow) {
                possibleValues.remove(value);
            }
        }
        return possibleValues;
    }

    public boolean solve() {
        for (int row = 0; row < Grid.GRID_SIZE; row++) {
            for (int col = 0; col < Grid.GRID_SIZE; col++) {
                if (grid.getCell(row, col) == 0) {
                    for (int num = 1; num <= 9; num++) {
                        if (getPossibleValues(row, col).contains(num)) {
                            grid.setCell(row, col, num);
                            if (solve()) {
                                return true;
                            }
                            grid.setCell(row, col, 0); // 回溯
                        }
                    }
                    return false;
                }
            }
        }
        return true;
    }

    public void printCandidates(Set<Integer>[][] candidates) {
        for (int row = 0; row < Grid.GRID_SIZE; row++) {
            for (int col = 0; col < Grid.GRID_SIZE; col++) {
                if (!candidates[row][col].isEmpty()) {
                    System.out.printf("Cell (%d, %d) candidates: %s%n", row, col, candidates[row][col]);
                }
            }
        }
    }

    public void printSudoku() {
        grid.printGrid();
    }

    public Sudoku clone() {
        return new Sudoku(grid.clone());
    }

    public String serialize() throws IOException {
        try (ByteArrayOutputStream bos = new ByteArrayOutputStream();
             ObjectOutputStream oos = new ObjectOutputStream(bos)) {
            oos.writeObject(this);
            return Base64.getEncoder().encodeToString(bos.toByteArray());
        }
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof Sudoku)) return false;
        Sudoku other = (Sudoku) obj;
        return grid.equals(other.grid);
    }
}
public class SudokuSolver {
    public static void main(String[] args) {
        String inputStr = "530070000600195000098000060800060003400803001700020006060000280000419005000080079";

        // 解析字符串，生成 Sudoku 实例
        Sudoku sudoku = Sudoku.parse(inputStr);

        // 获取数独推理候选值并输出
        System.out.println("Candidates for each empty cell:");
        Set<Integer>[][] candidates = sudoku.getInference();
        sudoku.printCandidates(candidates);

        // 求解数独并输出最终棋盘
        if (sudoku.solve()) {
            System.out.println("\nSolved Sudoku:");
            sudoku.printSudoku();
        } else {
            System.out.println("No solution exists.");
        }

        // 克隆数独实例并序列化
        try {
            Sudoku sudokuClone = sudoku.clone();
            System.out.println("\nSerialized Sudoku:");
            System.out.println(sudoku.serialize());

            // 比较原始和克隆的数独实例
            System.out.println("\n原始的和拷贝的数独实例一样吗? " + (sudoku.equals(sudokuClone)));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
