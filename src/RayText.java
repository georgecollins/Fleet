

public class RayText {
	
	/*
	 * This object uses sketch to draw a bunch of lines
	 * for letters.  
	 * It converts strings to drawings that it scales
	 * rotates, and does ticker effects,  
	 */
	static final int NUM_CHARACTERS = 41;  // A..Z SPACE 0..9 - > 
	static final int SPECIAL_CHARACTERS = 1;  // for now :
	sketch characters[];
	byte chardata[][];  // where the lines are
	void DrawLetter(int x, int y, int c) {
		// most basic implementation
		
		characters[c].drawat(x, y);
	}
	
	void DrawLetter(int x, int y, int c, double s, double f) {
		characters[c].drawrot(x, y, 0, s, f);
	}
	
	void ChangeFontColor(float r, float g, float b) {
		for (int i=0; i < NUM_CHARACTERS+ SPECIAL_CHARACTERS; i++)
			characters[i].ChangeColor(r, g, b);
	}
	
	void AddElement(sketch s, int n) {
		// add element # whatever to the sketch

		switch (n) {
		case 0:
			// the top horizontal bar
			s.AddLine(1, 20, 10, 20);
			break;
		case 1:
			// the middle horizontal bar
			s.AddLine(1, 10, 10, 10);
			break;
		case 2:
			// the bottom horizontal line
			s.AddLine(1, 1, 10, 1);
			break;
		case 3:
			// left bottom vertical
			s.AddLine(1, 1, 1, 10);
			break;
		case 4:
			// left top vertical
			s.AddLine(1, 10, 1, 20);
			break;
		case 5:
			// right bottom vertical
			s.AddLine(10, 1, 10, 10);
			break;
		case 6:
			// right top vertical 
			s.AddLine(10, 10, 10, 20);
			break;
		case 7: 
			// middle vertical
			s.AddLine(5, 1, 5, 20);
			break;
		case 8:
			// top \
			s.AddLine(1,20, 5, 10);
			break;
		case 9:
			// bottom \
			s.AddLine(5,10,10,0);
			break;
		case 10:
			// top /
			s.AddLine(10,20, 5, 10);
			break;
		case 11:
			// bottom /
			s.AddLine(5, 10, 1, 1);
			break;
		case 12:
			// half horizontal bottom for J
			s.AddLine(1, 1, 5, 1);
			break;
		case 13:
			// half vertical bottom for Y
			s.AddLine(5, 1, 5, 10);
			break;

			
		}
		s.ChangeColor(1.0f, 1.0f, 1.0f);
	}
	
	
	private void FillCharacters() {
		//n=0xFFFF;// how do I write binary or hexadceimal?
	//	int n=0b01;
		
		chardata= new byte[NUM_CHARACTERS][14];
	//	chardata[0]=1<<0 + 1<<1 + 1<<2 + 1<<3 + 1<<4;
		// A
		chardata[0][0]=1;
		chardata[0][1]=1;
		chardata[0][3]=1;
		chardata[0][4]=1;
		chardata[0][5]=1;
		chardata[0][6]=1;
		//B
		chardata[1][0]=1;
		chardata[1][1]=1;
		chardata[1][2]=1;
		chardata[1][5]=1;
		chardata[1][6]=1;
		chardata[1][7]=1;
		//C
		chardata[2][0]=1;
		chardata[2][2]=1;
		chardata[2][3]=1;
		chardata[2][4]=1;
		// D
		chardata[3][0]=1;
		chardata[3][2]=1;
		chardata[3][5]=1;
		chardata[3][6]=1;
		chardata[3][7]=1;
		// E
		chardata[4][0]=1;
		chardata[4][1]=1;
		chardata[4][2]=1;
		chardata[4][3]=1;
		chardata[4][4]=1;
		// F
		chardata[5][0]=1;
		chardata[5][1]=1;
		chardata[5][3]=1;
		chardata[5][4]=1;
		// G
		chardata[6][0]=1;
		chardata[6][2]=1;
		chardata[6][3]=1;
		chardata[6][4]=1;
		chardata[6][5]=1;
		// H
		chardata[7][1]=1;
		chardata[7][3]=1;
		chardata[7][4]=1;
		chardata[7][5]=1;
		chardata[7][6]=1;
		// I
		chardata[8][0]=1;
		chardata[8][2]=1;
		chardata[8][7]=1;
		// J
		chardata[9][0]=1;
		chardata[9][3]=1; 
		chardata[9][7]=1;
		chardata[9][12]=1;	
		// K
		chardata[10][7]=1;
		chardata[10][9]=1;
		chardata[10][10]=1;
		// L
		chardata[11][2]=1;
		chardata[11][3]=1;
		chardata[11][4]=1;
		// M
		chardata[12][3]=1;
		chardata[12][4]=1;
		chardata[12][5]=1;
		chardata[12][6]=1;
		chardata[12][8]=1;
		chardata[12][10]=1;
		// N
		chardata[13][3] = 1;
		chardata[13][4] = 1;
		chardata[13][5] = 1;
		chardata[13][6] = 1;
		chardata[13][8] = 1;
		chardata[13][9] = 1;
		// O
		chardata[14][0] = 1;
		chardata[14][2] = 1;
		chardata[14][3] = 1;
		chardata[14][4] = 1;
		chardata[14][5] = 1;
		chardata[14][6] = 1;
		// P 
		chardata[15][0] = 1;
		chardata[15][1] = 1;
		chardata[15][3] = 1;
		chardata[15][4] = 1;
		chardata[15][6] = 1;
		// Q
		chardata[16][0] = 1;
		chardata[16][2] = 1;
		chardata[16][3] = 1;
		chardata[16][4] = 1;
		chardata[16][5] = 1;
		chardata[16][6] = 1;
		chardata[16][9] = 1;
		// R
		chardata[17][0] = 1;
		chardata[17][1] = 1;
		chardata[17][3] = 1;
		chardata[17][4] = 1;
		chardata[17][6] = 1;			
		chardata[17][9] = 1;
		// S
		chardata[18][0] = 1;
		chardata[18][1] = 1;
		chardata[18][2] = 1;
		chardata[18][4] = 1;
		chardata[18][5] = 1;
		// T
		chardata[19][0] = 1;
		chardata[19][7] = 1;
		// U
		chardata[20][2] = 1;
		chardata[20][3] = 1;
		chardata[20][4] = 1;
		chardata[20][5] = 1;
		chardata[20][6] = 1;
		// V
		chardata[21][5] = 1;
		chardata[21][6] = 1;
		chardata[21][8] = 1;
		chardata[21][9] = 1;
		// W
		chardata[22][3] = 1;
		chardata[22][4] = 1;
		chardata[22][5] = 1;
		chardata[22][6] = 1;	
		chardata[22][9] = 1;
		chardata[22][11] = 1;
		// X
		chardata[23][8] = 1;
		chardata[23][9] = 1;
		chardata[23][10] = 1;
		chardata[23][11] = 1;
		// Y
		chardata[24][8] = 1;
		chardata[24][10] = 1;
		chardata[24][13] = 1;
		// Z
		chardata[25][0] = 1;
		chardata[25][2] = 1;
		chardata[25][10] = 1;
		chardata[25][11] = 1;
		// SPACE, , . ! etv.
		chardata[26][0] =0;
		// 1
		chardata[27][5] = 1;
		chardata[27][6] = 1;
		// 2
		chardata[28][0] = 1;
		chardata[28][1] = 1;
		chardata[28][2] = 1;
		chardata[28][3] = 1;
		chardata[28][6] = 1;
		// 3
		chardata[29][0] = 1;
		chardata[29][1] = 1;
		chardata[29][2] = 1;
		chardata[29][5] = 1;
		chardata[29][6] = 1;
		// 4 
		
		chardata[30][1] = 1;	
		chardata[30][4] = 1;
		chardata[30][5] = 1;
		chardata[30][6] = 1;
		// 5
		chardata[31][0] = 1;
		chardata[31][1] = 1;
		chardata[31][2] = 1;
		chardata[31][4] = 1;
		chardata[31][5] = 1;
		// 6
		chardata[32][1] = 1;
		chardata[32][2] = 1;
		chardata[32][3] = 1;
 		chardata[32][4] = 1;
 		chardata[32][5] = 1;	
 		// 7
 		chardata[33][0] = 1;
 		chardata[33][5] = 1;
 		chardata[33][6] = 1;
 		//8
 		chardata[34][0] = 1;
 		chardata[34][1] = 1;
 		chardata[34][2] = 1;
 		chardata[34][3] = 1;
 		chardata[34][4] = 1;
 		chardata[34][5] = 1;
 		chardata[34][6] = 1;
 		// 9
 		chardata[35][0] = 1;
 		chardata[35][1] = 1;
 		chardata[35][4] = 1;
 		chardata[35][5] = 1;
 		chardata[35][6] = 1;
 		// 0
 		chardata[36][0] = 1;
 		chardata[36][2] = 1;
 		chardata[36][3] = 1;
 		chardata[36][4] = 1;
 		chardata[36][5] = 1;
 		chardata[36][6] = 1;
 		
 		// - 
 		chardata[37][1] = 1;
 		
 		// >
 		chardata[38][8] = 1;
 		chardata[38][11] = 1;
 		
 		//  /
 		chardata[39][8] = 1;
 		chardata[39][9] = 1;
 		// \
 		chardata[40][10] = 1;
 		chardata[40][11] = 1;
 		
 		for (int count=0; count < 14; count=count + 1)
			chardata[26][count] = 1;
		
		
		
	}
	
	void DrawText(int x, int y, String s) {
		int atx= x;
		int aty= y;
		char[] chars = s.toCharArray();
		//System.out.print(s.length());
		for (int count=0; count < s.length(); count++) {
			String m=s.substring(count, count+1);
	// I should Repalce this with a string compare "ABCDEFGHIJHKLMNOPQRSTUVWXYZ"
			if (m.equals("A")) DrawLetter(atx, aty, 0);		
			if (m.equals("B")) DrawLetter(atx, aty, 1);
			if (m.equals("C")) DrawLetter(atx, aty, 2);
			if (m.equals("D")) DrawLetter(atx, aty, 3);
			if (m.equals("E")) DrawLetter(atx, aty, 4);		
			if (m.equals("F")) DrawLetter(atx, aty, 5);
			if (m.equals("G")) DrawLetter(atx, aty, 6);
			if (m.equals("H")) DrawLetter(atx, aty, 7);
			if (m.equals("I")) DrawLetter(atx, aty, 8);		
			if (m.equals("J")) DrawLetter(atx, aty, 9);
			if (m.equals("K")) DrawLetter(atx, aty, 10);
			if (m.equals("L")) DrawLetter(atx, aty, 11);
			if (m.equals("M")) DrawLetter(atx, aty, 12);		
			if (m.equals("N")) DrawLetter(atx, aty, 13);
			if (m.equals("O")) DrawLetter(atx, aty, 14);
			if (m.equals("P")) DrawLetter(atx, aty, 15);
			if (m.equals("Q")) DrawLetter(atx, aty, 16);		
			if (m.equals("R")) DrawLetter(atx, aty, 17);
			if (m.equals("S")) DrawLetter(atx, aty, 18);
			if (m.equals("T")) DrawLetter(atx, aty, 19);
			if (m.equals("U")) DrawLetter(atx, aty, 20);		
			if (m.equals("V")) DrawLetter(atx, aty, 21);
			if (m.equals("W")) DrawLetter(atx, aty, 22);
			if (m.equals("X")) DrawLetter(atx, aty, 23);
			if (m.equals("Y")) DrawLetter(atx, aty, 24);		
			if (m.equals("Z")) DrawLetter(atx, aty, 25);
			if (m.equals("1")) DrawLetter(atx, aty, 27);
			if (m.equals("2")) DrawLetter(atx, aty, 28);
			if (m.equals("3")) DrawLetter(atx, aty, 29);
			if (m.equals("4")) DrawLetter(atx, aty, 30);
			if (m.equals("5")) DrawLetter(atx, aty, 31);
			if (m.equals("6")) DrawLetter(atx, aty, 32);
			if (m.equals("7")) DrawLetter(atx, aty, 33);
			if (m.equals("8")) DrawLetter(atx, aty, 34);
			if (m.equals("9")) DrawLetter(atx, aty, 35);
			if (m.equals("0")) DrawLetter(atx, aty, 36);
			if (m.equals("-")) DrawLetter(atx, aty, 37);
			if (m.equals(">")) DrawLetter(atx, aty, 38);
//			if (m.equals("\")) DrawLetter(atx, aty, 39, sc, fade);
			if (m.equals("/")) DrawLetter(atx, aty, 40);;
			
			if (m.equals(":")) DrawLetter(atx, aty, NUM_CHARACTERS+0);
		


		

			atx=atx+12;
		}
	}
	
	
	public void DrawFadeText(int x, int y, String s, double start, double end) {
		int atx= x;
		int aty= y;
		char[] chars = s.toCharArray();
		double fade;
		//System.out.print(s.length());
		for (int count=0; count < s.length(); count++) {
			fade=1.0d;
			if (atx < start) {
				fade = 1.0d-(start-atx)/100d;
			
			}
			if (atx> end) {
				fade = 1.0d- (atx-end)/100d;
			}
			if (fade < 0.0d) fade=0.0d;
			String m=s.substring(count, count+1);
	// I should Replace this with a string compare "ABCDEFGHIJHKLMNOPQRSTUVWXYZ"
			if (m.equals("A")) DrawLetter(atx, aty, 0, 1.0f, fade);		
			if (m.equals("B")) DrawLetter(atx, aty, 1, 1.0f, fade);
			if (m.equals("C")) DrawLetter(atx, aty, 2, 1.0f, fade);
			if (m.equals("D")) DrawLetter(atx, aty, 3, 1.0f, fade);
			if (m.equals("E")) DrawLetter(atx, aty, 4, 1.0f, fade);		
			if (m.equals("F")) DrawLetter(atx, aty, 5, 1.0f, fade);
			if (m.equals("G")) DrawLetter(atx, aty, 6, 1.0f, fade);
			if (m.equals("H")) DrawLetter(atx, aty, 7, 1.0f, fade);
			if (m.equals("I")) DrawLetter(atx, aty, 8, 1.0f, fade);		
			if (m.equals("J")) DrawLetter(atx, aty, 9, 1.0f, fade);
			if (m.equals("K")) DrawLetter(atx, aty, 10, 1.0f, fade);
			if (m.equals("L")) DrawLetter(atx, aty, 11, 1.0f, fade);
			if (m.equals("M")) DrawLetter(atx, aty, 12, 1.0f, fade);		
			if (m.equals("N")) DrawLetter(atx, aty, 13, 1.0f, fade);
			if (m.equals("O")) DrawLetter(atx, aty, 14, 1.0f, fade);
			if (m.equals("P")) DrawLetter(atx, aty, 15, 1.0f, fade);
			if (m.equals("Q")) DrawLetter(atx, aty, 16, 1.0f, fade);		
			if (m.equals("R")) DrawLetter(atx, aty, 17, 1.0f, fade);
			if (m.equals("S")) DrawLetter(atx, aty, 18, 1.0f, fade);
			if (m.equals("T")) DrawLetter(atx, aty, 19, 1.0f, fade);
			if (m.equals("U")) DrawLetter(atx, aty, 20, 1.0f, fade);		
			if (m.equals("V")) DrawLetter(atx, aty, 21, 1.0f, fade);
			if (m.equals("W")) DrawLetter(atx, aty, 22, 1.0f, fade);
			if (m.equals("X")) DrawLetter(atx, aty, 23, 1.0f, fade);
			if (m.equals("Y")) DrawLetter(atx, aty, 24, 1.0f, fade);		
			if (m.equals("Z")) DrawLetter(atx, aty, 25, 1.0f, fade);
			if (m.equals("1")) DrawLetter(atx, aty, 27, 1.0f, fade);
			if (m.equals("2")) DrawLetter(atx, aty, 28, 1.0f, fade);
			if (m.equals("3")) DrawLetter(atx, aty, 29, 1.0f, fade);
			if (m.equals("4")) DrawLetter(atx, aty, 30, 1.0f, fade);
			if (m.equals("5")) DrawLetter(atx, aty, 31, 1.0f, fade);
			if (m.equals("6")) DrawLetter(atx, aty, 32, 1.0f, fade);
			if (m.equals("7")) DrawLetter(atx, aty, 33, 1.0f, fade);
			if (m.equals("8")) DrawLetter(atx, aty, 34, 1.0f, fade);
			if (m.equals("9")) DrawLetter(atx, aty, 35, 1.0f, fade);
			if (m.equals("0")) DrawLetter(atx, aty, 36, 1.0f, fade);

			if (m.equals("-")) DrawLetter(atx, aty, 37, 1.0f, fade);
			if (m.equals(">")) DrawLetter(atx, aty, 38, 1.0f, fade);
//;			if (m.equals("\")) DrawLetter(atx, aty, 39, 1.0f, fade);
			if (m.equals("/")) DrawLetter(atx, aty, 40, 1.0f, fade);
			
			if (m.equals(":")) DrawLetter(atx, aty, NUM_CHARACTERS+0, 1.0f, fade);
	
		

			atx=atx+13;
		}		
	}

	
	public void DrawScaleText(int x, int y, String s, double sc) {
		int atx= x;
		int aty= y;
		char[] chars = s.toCharArray();
		//System.out.print(s.length());
		for (int count=0; count < s.length(); count++) {
			double fade =1.0d;
			String m=s.substring(count, count+1);
	// I should Replace this with a string compare "ABCDEFGHIJHKLMNOPQRSTUVWXYZ"
			if (m.equals("A")) DrawLetter(atx, aty, 0, sc, fade);		
			if (m.equals("B")) DrawLetter(atx, aty, 1, sc, fade);
			if (m.equals("C")) DrawLetter(atx, aty, 2, sc, fade);
			if (m.equals("D")) DrawLetter(atx, aty, 3, sc, fade);
			if (m.equals("E")) DrawLetter(atx, aty, 4, sc, fade);		
			if (m.equals("F")) DrawLetter(atx, aty, 5, sc, fade);
			if (m.equals("G")) DrawLetter(atx, aty, 6, sc, fade);
			if (m.equals("H")) DrawLetter(atx, aty, 7, sc, fade);
			if (m.equals("I")) DrawLetter(atx, aty, 8, sc, fade);		
			if (m.equals("J")) DrawLetter(atx, aty, 9, sc, fade);
			if (m.equals("K")) DrawLetter(atx, aty, 10, sc, fade);
			if (m.equals("L")) DrawLetter(atx, aty, 11, sc, fade);
			if (m.equals("M")) DrawLetter(atx, aty, 12, sc, fade);		
			if (m.equals("N")) DrawLetter(atx, aty, 13, sc, fade);
			if (m.equals("O")) DrawLetter(atx, aty, 14, sc, fade);
			if (m.equals("P")) DrawLetter(atx, aty, 15, sc, fade);
			if (m.equals("Q")) DrawLetter(atx, aty, 16, sc, fade);		
			if (m.equals("R")) DrawLetter(atx, aty, 17, sc, fade);
			if (m.equals("S")) DrawLetter(atx, aty, 18, sc, fade);
			if (m.equals("T")) DrawLetter(atx, aty, 19, sc, fade);
			if (m.equals("U")) DrawLetter(atx, aty, 20, sc, fade);		
			if (m.equals("V")) DrawLetter(atx, aty, 21, sc, fade);
			if (m.equals("W")) DrawLetter(atx, aty, 22, sc, fade);
			if (m.equals("X")) DrawLetter(atx, aty, 23, sc, fade);
			if (m.equals("Y")) DrawLetter(atx, aty, 24, sc, fade);		
			if (m.equals("Z")) DrawLetter(atx, aty, 25, sc, fade);
			if (m.equals("1")) DrawLetter(atx, aty, 27, sc, fade);
			if (m.equals("2")) DrawLetter(atx, aty, 28, sc, fade);
			if (m.equals("3")) DrawLetter(atx, aty, 29, sc, fade);
			if (m.equals("4")) DrawLetter(atx, aty, 30, sc, fade);
			if (m.equals("5")) DrawLetter(atx, aty, 31, sc, fade);
			if (m.equals("6")) DrawLetter(atx, aty, 32, sc, fade);
			if (m.equals("7")) DrawLetter(atx, aty, 33, sc, fade);
			if (m.equals("8")) DrawLetter(atx, aty, 34, sc, fade);
			if (m.equals("9")) DrawLetter(atx, aty, 35, sc, fade);
			if (m.equals("0")) DrawLetter(atx, aty, 36, sc, fade);

			if (m.equals("-")) DrawLetter(atx, aty, 37, sc, fade);
			if (m.equals(">")) DrawLetter(atx, aty, 38, sc, fade);
//			if (m.equals("\")) DrawLetter(atx, aty, 39, sc, fade);
			if (m.equals("/")) DrawLetter(atx, aty, 40, sc, fade);
			
			if (m.equals(":")) DrawLetter(atx, aty, NUM_CHARACTERS+0, sc, fade);
		

			atx=atx+(int)(13*sc);
		}		
	}
	
	int GetLength(String s, double scale) {	
		return s.length()*((int) (12*scale));
	}
	
	int GetHeight(double scale) {
		return ((int) (24d*scale));
	}
	
	RayText() {
		// initialize the letters
		// 0 = A , etc
		
		characters= new sketch[NUM_CHARACTERS + SPECIAL_CHARACTERS];
		FillCharacters();
		for (int count=0; count < NUM_CHARACTERS; count++) {
			characters[count]=new sketch();
			/*
			int mask=2^0;
			for (int loop=0; loop <12; loop++) {
				mask=1<<loop;
				int result= mask & chardata[count];
				System.out.println(chardata[count] + " mask "+ mask + " boolean & " + result);//Math.pow(2,loop));
				if ((chardata[count] & (1<<loop))>0) { // 2^loop is the mask

					System.out.println("test");
					AddElement(characters[count], loop);
	
				}
			}
			*/
			for (int loop=0; loop<14; loop++) 
				if (chardata[count][loop]>0)
					AddElement(characters[count], loop);
		}
		
		// Special Characters
		// : 
		characters[NUM_CHARACTERS] = new sketch();
		characters[NUM_CHARACTERS].numpoints= 4;
		characters[NUM_CHARACTERS].xmat[0] = 5;
		characters[NUM_CHARACTERS].ymat[0] = 2;
		characters[NUM_CHARACTERS].xmat[1] = 5;
		characters[NUM_CHARACTERS].ymat[1] = 7;
		
		characters[NUM_CHARACTERS].xmat[2] = 5;
		characters[NUM_CHARACTERS].ymat[2] = 12;
		characters[NUM_CHARACTERS].xmat[3] = 5;
		characters[NUM_CHARACTERS].ymat[3] = 17;
		
		for (int count = 0; count < characters[NUM_CHARACTERS].numpoints/2; count++) {
			characters[NUM_CHARACTERS].rcolor[count] = 0.9f;
			characters[NUM_CHARACTERS].gcolor[count] = 0.9f;
			characters[NUM_CHARACTERS].bcolor[count] = 0.9f;
			characters[NUM_CHARACTERS].width[count] = 2.0f;  // wide lines
		}
		
		
		
	}
}
