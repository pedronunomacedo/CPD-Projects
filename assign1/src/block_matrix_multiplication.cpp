#include <stdio.h>
#include <iostream>
#include <iomanip>
#include <string>
#include <time.h>
#include <cstdlib>
#include <papi.h>

using namespace std;

#define SYSTEMTIME clock_t

void displayMatrixResult(int m_ar, int m_br, double *phc) {
    int num_elems = min(10, m_ar * m_br), curr_elem = 0, line = 0, col = 0;
	cout << "\nResult matrix (phc): " << endl;
	while (curr_elem < num_elems) {
		if (col == m_ar) {
			col = 0;
			line++;
			cout << endl;
		}
		cout << phc[line * m_br + col] << " ";
		col++;
		curr_elem++;
	} 
	cout << endl << endl;
}

void constructMatrixes(int m_ar, int m_br, double *pha, double *phb) {
    for(int i = 0; i < m_ar; i++)
		for(int j = 0; j < m_ar; j++)
			pha[i*m_ar + j] = (double)1.0;

	for(int i = 0; i < m_br; i++)
		for(int j = 0; j < m_br; j++)
			phb[i * m_br + j] = (double)(i + 1);
}

 
void OnMult(int m_ar, int m_br)  {
	
	SYSTEMTIME Time1, Time2;
	
	char st[100];
	double temp;
	int i, j, k;

	double *pha, *phb, *phc;
	

	
    pha = (double *)malloc((m_ar * m_ar) * sizeof(double));
	phb = (double *)malloc((m_ar * m_ar) * sizeof(double));
	phc = (double *)malloc((m_ar * m_ar) * sizeof(double));

	constructMatrixes(m_ar, m_br, pha, phb);

    Time1 = clock();

	// Loops  through the result matrix phc
	for(i=0; i<m_ar; i++) {	
		for( j=0; j<m_br; j++) {
			// Element (i, j) of phc -> calcules of 2 lines of the matrix
			temp = 0;
			for( k=0; k<m_ar; k++) {	
				temp += pha[i*m_ar+k] * phb[k*m_br+j];
			}
			phc[i*m_ar+j]=temp;
		}
	}

    Time2 = clock();
	sprintf(st, "Time: %3.3f seconds\n", (double)(Time2 - Time1) / CLOCKS_PER_SEC);
	cout << st;

	displayMatrixResult(m_ar, m_br, phc);

    free(pha);
    free(phb);
    free(phc);
	
}

// add code here for line x line matriz multiplication
void OnMultLine(int m_ar, int m_br) {
    SYSTEMTIME Time1, Time2;
	
	char st[100];
	double temp;
	int i, j, k;

	double *pha, *phb, *phc;
	

	
    pha = (double *)malloc((m_ar * m_ar) * sizeof(double));
	phb = (double *)malloc((m_ar * m_ar) * sizeof(double));
	phc = (double *)malloc((m_ar * m_ar) * sizeof(double));

	constructMatrixes(m_ar, m_br, pha, phb);

	Time1 = clock();

	// Loops  through the result matrix phc
	for (int line_pha = 0; line_pha < m_ar; line_pha++) {
		for (int elem_line_pha = 0; elem_line_pha < m_br; elem_line_pha++) {
			for (int elem_col_phb = 0; elem_col_phb < m_br; elem_col_phb++) {
				phc[line_pha * m_br + elem_col_phb] += pha[line_pha * m_ar + elem_line_pha] * phb[elem_line_pha * m_br + elem_col_phb];
			}
		}
	}

	Time2 = clock();
	sprintf(st, "Time: %3.3f seconds\n", (double)(Time2 - Time1) / CLOCKS_PER_SEC);
	cout << st;

	displayMatrixResult(m_ar, m_br, phc);

    free(pha);
    free(phb);
    free(phc);
}

// add code here for block x block matriz multiplication
void OnMultBlock(int m_ar, int m_br, int bkSize) {
	if (((m_ar % bkSize) != 0) || ((m_br % bkSize) != 0)) {
		cout << "\nERROR: The size of the block(" + to_string(bkSize) + ") is not a divisor of the matriz size(" + to_string(m_ar) + ")!\n\n" << endl;
		return;
	}

	SYSTEMTIME Time1, Time2;
    
    char st[100];
    double temp;
    int i, j, k;

    double *pha, *phb, *phc;
    
    pha = (double *)malloc((m_ar * m_ar) * sizeof(double));
    phb = (double *)malloc((m_ar * m_ar) * sizeof(double));
    phc = (double *)malloc((m_ar * m_ar) * sizeof(double));

	constructMatrixes(m_ar, m_br, pha, phb);

	Time1 = clock();

	for(int line_block_a = 0; line_block_a < m_ar; line_block_a += bkSize){
		for(int col_block_a = 0; col_block_a < m_ar; col_block_a +=  bkSize){
			for(int col_block_b = 0; col_block_b < m_ar; col_block_b += bkSize){
				l
					for(int col_a = 0; col_a < bkSize; col_a++){ // element of the line
						for(int col_b = 0; col_b < bkSize; col_b++){ // element correpondent on matrix phb
							phc[(line_block_a + line_a) * m_ar + (col_block_a + col_b)]  += pha[(line_block_a + line_a) * m_ar + (col_block_b + col_a)] * phb[(col_block_b + col_a) * m_ar + (col_block_a + col_b)];
						}
					}
				}
			}
		}
	}

	Time2 = clock();
	sprintf(st, "Time: %3.3f seconds\n", (double)(Time2 - Time1) / CLOCKS_PER_SEC);
	cout << st;

	displayMatrixResult(m_ar, m_br, phc);

    free(pha);
    free(phb);
    free(phc);
}



void handle_error (int retval) {
  printf("PAPI error %d: %s\n", retval, PAPI_strerror(retval));
  exit(1);
}

void init_papi() {
  int retval = PAPI_library_init(PAPI_VER_CURRENT);
  if (retval != PAPI_VER_CURRENT && retval < 0) {
    printf("PAPI library version mismatch!\n");
    exit(1);
  }
  if (retval < 0) handle_error(retval);

  std::cout << "PAPI Version Number: MAJOR: " << PAPI_VERSION_MAJOR(retval)
            << " MINOR: " << PAPI_VERSION_MINOR(retval)
            << " REVISION: " << PAPI_VERSION_REVISION(retval) << "\n";
}


int main (int argc, char *argv[])
{

	char c;
	int lin, col, blockSize;
	int op;
	
	int EventSet = PAPI_NULL;
  	long long values[2];
  	int ret;
	

	ret = PAPI_library_init( PAPI_VER_CURRENT );
	if ( ret != PAPI_VER_CURRENT )
		std::cout << "FAIL" << endl;


	ret = PAPI_create_eventset(&EventSet);
		if (ret != PAPI_OK) cout << "ERROR: create eventset" << endl;


	ret = PAPI_add_event(EventSet,PAPI_L1_DCM );
	if (ret != PAPI_OK) cout << "ERROR: PAPI_L1_DCM" << endl;

	ret = PAPI_add_event(EventSet,PAPI_L2_DCM);
	if (ret != PAPI_OK) cout << "ERROR: PAPI_L2_DCM" << endl;



	
	ret = PAPI_add_event(EventSet,PAPI_L2_DCA);
	if (ret != PAPI_OK) cout << "ERROR: PAPI_L2_DCA" << endl;

	ret = PAPI_add_event(EventSet,PAPI_L3_DCA);
	if (ret != PAPI_OK) cout << "ERROR: PAPI_L3_DCA" << endl;





	op=1;
	do {
		int validInput = false;
		cout << endl << "1. Multiplication" << endl;
		cout << "2. Line Multiplication" << endl;
		cout << "3. Block Multiplication" << endl;
		cout << "Selection?: ";

		while (!validInput) {
            cin >> op;
            if (op == 0 || op == 1 || op == 2 || op == 3) {
                validInput = true;
            }
            else {
                cout << "Invalid input! PLease try again: ";
            }
        }
		
		if (op == 0) break; // Exit program

		int validDimension = false;
        printf("Dimensions: lins=cols ? ");
        while (!validDimension) {
   		    cin >> lin;
            if (lin >= 0) {
                validDimension = true;
            } else {
                cout << "Invalid dimension! Please try again (dimension >= 0): ";
            }
        }

		col = lin;

		// Start counting
		ret = PAPI_start(EventSet);
		if (ret != PAPI_OK) cout << "ERROR: Start PAPI" << endl;

		switch (op){
			case 1: 
				OnMult(lin, col);
				break;
			case 2:
				OnMultLine(lin, col);  
				break;
			case 3:
				cout << "Block Size? ";
				cin >> blockSize;
				OnMultBlock(lin, col, blockSize);  
				break;

		}

  		ret = PAPI_stop(EventSet, values);
  		if (ret != PAPI_OK) cout << "ERROR: Stop PAPI" << endl;
  		printf("L1 DCM: %lld \n",values[0]);
  		printf("L2 DCM: %lld \n",values[1]);
		printf("L2 DCA: %lld \n",values[2]);
		printf("L3 DCA: %lld \n",values[3]);

		ret = PAPI_reset( EventSet );
		if ( ret != PAPI_OK )
			std::cout << "FAIL reset" << endl; 



	}while (op != 0);

	ret = PAPI_remove_event( EventSet, PAPI_L1_DCM );
	if ( ret != PAPI_OK )
		std::cout << "FAIL remove event" << endl; 

	ret = PAPI_remove_event( EventSet, PAPI_L2_DCM );
	if ( ret != PAPI_OK )
		std::cout << "FAIL remove event" << endl; 




	ret = PAPI_remove_event( EventSet, PAPI_L2_DCA );
	if ( ret != PAPI_OK )
		std::cout << "FAIL remove event" << endl; 

	ret = PAPI_remove_event( EventSet, PAPI_L3_DCA );
	if ( ret != PAPI_OK )
		std::cout << "FAIL remove event" << endl; 






	ret = PAPI_destroy_eventset( &EventSet );
	if ( ret != PAPI_OK )
		std::cout << "FAIL destroy" << endl;

}