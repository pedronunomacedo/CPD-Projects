import time

def displayMatrixResult(m_ar, m_br, phc):
    num_elems = min(10, m_ar * m_br)
    curr_elem = 0
    line = 0
    col = 0
    print("\nResult matrix (phc): ")
    while curr_elem < num_elems:
        if col == m_ar:
            col = 0
            line += 1
            print()
        print(phc[line][col], end=' ')
        col += 1
        curr_elem += 1
    print("\n")


def constructMatrixes(m_ar, m_br):
    pha = [[1.0 for j in range(m_br)] for i in range(m_ar)]
    phb = [[float(i+1) for j in range(m_br)] for i in range(m_ar)]
    phc = [[0 for j in range(m_br)] for i in range(m_ar)]
    return pha, phb, phc

def OnMult(m_ar, m_br):

    pha, phb, phc = constructMatrixes(m_ar, m_br)

    Time1 = time.time()

    for i in range(m_ar):
        for j in range(m_br):
            temp = 0
            for k in range(m_ar):
                temp += pha[i][k] * phb[k][j]
            phc[i][j] = temp

    Time2 = time.time()
    print("Time: {:.5f} seconds\n".format(Time2 - Time1))
    
    displayMatrixResult(m_ar, m_br, phc)

def OnMultLine(m_ar, m_br):
    pha, phb, phc = constructMatrixes(m_ar, m_br)

    Time1 = time.time()

    # Loops through the result matrix phc
    for line_pha in range(m_ar):
        for elem_line_pha in range(m_br):
            for elem_col_phb in range(m_br):
                phc[line_pha][elem_col_phb] += pha[line_pha][elem_line_pha] * phb[elem_line_pha][elem_col_phb]

    Time2 = time.time()
    print("Time: {:.5f} seconds\n".format(Time2 - Time1))

    displayMatrixResult(m_ar, m_br, phc)

def OnMultBlock(m_ar, m_br, bkSize):
    if m_ar % bkSize != 0 or m_br % bkSize != 0:
        print(f"\nERROR: The size of the block({bkSize}) is not a divisor of the matrix size({m_ar})!\n\n")
        return

    pha, phb, phc = constructMatrixes(m_ar, m_br)

    Time1 = time.time()

    for line_block_a in range(0, m_ar, bkSize):
        for col_block_a in range(0, m_ar, bkSize):
            for col_block_b in range(0, m_ar, bkSize):
                for line_a in range(bkSize):
                    for col_a in range(bkSize):
                        for col_b in range(bkSize):
                            phc[line_block_a + line_a] [col_block_a + col_b] += pha[line_block_a + line_a] [col_block_b + col_a] * phb[col_block_b + col_a][col_block_a + col_b]

    Time2 = time.time()
    print("Time: {:.5f} seconds\n".format(Time2 - Time1))

    displayMatrixResult(m_ar, m_br, phc)

def main():
    op = 1
    while True:
        validInput = False
        print("\n1. Multiplication")
        print("2. Line Multiplication")
        print("3. Block Multiplication")
        print("0. Exit")
        op = int(input("Selection?: "))
        while not validInput:
            if op in [0, 1, 2, 3]:
                validInput = True
            else:
                print("Invalid input! Please try again: ")
                op = int(input("Selection?: "))
        if op == 0:
            break # Exit program
        validDimension = False
        lin = int(input("Dimensions: lins=cols ? "))
        while not validDimension:
            if lin >= 0:
                validDimension = True
            else:
                print("Invalid dimension! Please try again (dimension >= 0): ")
                lin = int(input("Dimensions: lins=cols ? "))
        col = lin
        if op == 1:
            OnMult(lin, col)
        if op == 2:
            OnMultLine(lin, col)
        if op == 3:
            blockSize = int(input("Block Size? "))
            OnMultBlock(lin, col, blockSize)




    return 1


if __name__ == '__main__':
    main()