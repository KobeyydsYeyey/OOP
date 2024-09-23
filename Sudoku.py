import numpy as np
import copy
import json

# 定义 Grid 类，用于存储数独棋盘及相关操作
class Grid:
    BOX_SIZE = 3  # 每个方块的大小 (3x3)
    GRID_SIZE = 9 # 整个数独棋盘大小 (9x9)

    def __init__(self, grid=None):
        # 初始化数独棋盘
        if grid is None:
            self.grid = np.zeros((Grid.GRID_SIZE, Grid.GRID_SIZE), dtype=int)
        else:
            self.grid = np.array(grid)

    # 获取特定行的数据
    def get_row(self, row):
        return self.grid[row]

    # 获取特定列的数据
    def get_column(self, col):
        return self.grid[:, col]

    # 获取特定3x3方块的数据
    def get_box(self, row, col):
        start_row = (row // Grid.BOX_SIZE) * Grid.BOX_SIZE
        start_col = (col // Grid.BOX_SIZE) * Grid.BOX_SIZE
        return self.grid[start_row:start_row + Grid.BOX_SIZE, start_col:start_col + Grid.BOX_SIZE]

    # 设置某个位置的数字
    def set_cell(self, row, col, value):
        self.grid[row][col] = value

    # 获取某个位置的数字
    def get_cell(self, row, col):
        return self.grid[row][col]

    # 打印数独棋盘
    def print_grid(self):
        for row in self.grid:
            print(row)

    # 序列化为 JSON
    def serialize(self):
        return json.dumps(self.grid.tolist())

    # 克隆当前对象
    def clone(self):
        return Grid(self.grid.copy())

# 定义 Sudoku 类，用于解析输入字符串并获取推理结果和求解
class Sudoku:
    def __init__(self, grid):
        self.grid = grid

    # 解析字符串输入，返回一个 Sudoku 实例
    @staticmethod
    def parse(input_str):
        grid_data = np.zeros((Grid.GRID_SIZE, Grid.GRID_SIZE), dtype=int)
        for i, char in enumerate(input_str):
            row = i // Grid.GRID_SIZE
            col = i % Grid.GRID_SIZE
            grid_data[row][col] = int(char)
        return Sudoku(Grid(grid_data))

    # 获取数独的推理结果，返回一个二维数组，每个单元格的候选值
    def get_inference(self):
        candidates = [[set() for _ in range(Grid.GRID_SIZE)] for _ in range(Grid.GRID_SIZE)]
        for row in range(Grid.GRID_SIZE):
            for col in range(Grid.GRID_SIZE):
                if self.grid.get_cell(row, col) == 0:
                    candidates[row][col] = self.get_possible_values(row, col)
        return candidates

    # 获取某个单元格的候选值集合
    def get_possible_values(self, row, col):
        possible_values = set(range(1, 10))
        possible_values -= set(self.grid.get_row(row))  # 排除当前行的数字
        possible_values -= set(self.grid.get_column(col))  # 排除当前列的数字
        possible_values -= set(self.grid.get_box(row, col).flatten())  # 排除当前3x3方块的数字
        possible_values.discard(0)  # 移除 0 (空单元格)
        return possible_values

    # 使用回溯法求解数独并返回结果
    def solve(self):
        for row in range(Grid.GRID_SIZE):
            for col in range(Grid.GRID_SIZE):
                if self.grid.get_cell(row, col) == 0:
                    for num in range(1, 10):
                        if num in self.get_possible_values(row, col):
                            self.grid.set_cell(row, col, num)
                            if self.solve():
                                return True
                            else:
                                self.grid.set_cell(row, col, 0)  # 回溯
                    return False
        return True

    # 打印候选值
    def print_candidates(self, candidates):
        for row in range(Grid.GRID_SIZE):
            for col in range(Grid.GRID_SIZE):
                if candidates[row][col]:
                    print(f"Cell ({row}, {col}) candidates: {candidates[row][col]}")

    # 打印数独棋盘
    def print_sudoku(self):
        self.grid.print_grid()

    # 克隆当前对象
    def clone(self):
        return Sudoku(self.grid.clone())

    # 序列化为 JSON
    def serialize(self):
        return self.grid.serialize()

    # 比较两个 Sudoku 对象
    def __eq__(self, other):
        if not isinstance(other, Sudoku):
            return False
        return np.array_equal(self.grid.grid, other.grid.grid)

# 测试代码
if __name__ == "__main__":
    input_str = "530070000600195000098000060800060003400803001700020006060000280000419005000080079"
    
    # 解析字符串，生成 Sudoku 实例
    sudoku = Sudoku.parse(input_str)

    # 获取数独推理候选值并输出
    print("Candidates for each empty cell:")
    candidates = sudoku.get_inference()
    sudoku.print_candidates(candidates)

    # 求解数独并输出最终棋盘
    if sudoku.solve():
        print("\nSolved Sudoku:")
        sudoku.print_sudoku()
    else:
        print("No solution exists.")

    # 克隆数独实例并序列化
    sudoku_clone = sudoku.clone()
    print("\nSerialized Sudoku:")
    print(sudoku.serialize())

    # 比较原始和克隆的数独实例
    print("\n原始的和拷贝的数独实例一样吗?", sudoku == sudoku_clone)
