/**
	Scrivere un programma YASPL.2 che visualizzi un menu in cui un utente:
		1. La somma di due numeri
		2. La moltiplicazione di due numeri utilizzando la somma
		3. La divisione intera fra due numeri positivi
		4. L’elevamento a potenza
		5. La successione di Fibonacci
	
	@author Antonio Tino
 */

package yaspl2cc;

import Parser.ParserCup;

import java.io.FileInputStream;

import LexicalAnalyzer.Lexer;;

public class Main {

	public static void main(String[] args){
		
		try {
			
			ParserCup pc = new ParserCup(new Lexer(new FileInputStream(args[0])));
			if(args.length == 2)
				pc.setNameFile(args[1]);
				
			pc.parse();
			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}