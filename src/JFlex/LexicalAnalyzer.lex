/* Implementazione dell'analizzatore lessicale per YASPL.2 con JFlex */

package LexicalAnalyzer;
import java_cup.runtime.*;
import Parser.*;
import Table.*;

%%

/* nome della classe che genera JFlex */
%class Lexer 
%public
%unicode
%cup
%line
%column

%{ 
 StringBuffer string = new StringBuffer();
 StringTable symbolTable = StringTable.getInstance();
 
 private Symbol symbol(int type) {
	return new Symbol(type, yyline, yycolumn);
 }
 
 private Symbol symbol(int type, Object value) {
    return new Symbol(type, yyline, yycolumn, value);
 }
 
 private Symbol installID(String lexem){
 	if(!symbolTable.contains(lexem))
 		symbolTable.add(lexem);
 	return symbol(ParserSym.NAME, symbolTable.indexOf(lexem));
 }
 
%}

LineTerminator 								= \r|\n|\r\n
InputCharacter 								= [^\r\n]
WhiteSpace									= {LineTerminator} | [ \t\f]
	
/* Commenti */
Comment										= {TraditionalComment} | {EndOfLineComment} 
TraditionalComment 							= "/*" ~"*/"
EndOfLineComment		 					= "//" {InputCharacter}* {LineTerminator}?

/* Espressione regolare per gli identificatori */
Name 										= [:jletter:] [:jletterdigit:]*

/* Espressione regolare per i numeri interi e con la virgola */
Digit										= [0-9]
Digits 										= [1-9]
DecIntegerLiteral 							= 0 | {Digits}{Digit}*
NoIntegerLiterl 							= {DecIntegerLiteral}(\.?{Digit}+)

%state STRING

%%

/* Keyword */
<YYINITIAL> "head" 							{ return symbol(ParserSym.HEAD); }
<YYINITIAL> "start" 						{ return symbol(ParserSym.START); }
<YYINITIAL> "int" 							{ return symbol(ParserSym.INT); }
<YYINITIAL> "bool" 							{ return symbol(ParserSym.BOOL); }
<YYINITIAL> "double" 						{ return symbol(ParserSym.DOUBLE); }
<YYINITIAL> "def" 							{ return symbol(ParserSym.DEF); }
<YYINITIAL> "true" 							{ return symbol(ParserSym.TRUE); }
<YYINITIAL> "false" 						{ return symbol(ParserSym.FALSE); }
<YYINITIAL> "if" 							{ return symbol(ParserSym.IF); }
<YYINITIAL> "then" 							{ return symbol(ParserSym.THEN); }
<YYINITIAL> "while" 						{ return symbol(ParserSym.WHILE); }
<YYINITIAL> "do" 							{ return symbol(ParserSym.DO); }
<YYINITIAL> "else" 							{ return symbol(ParserSym.ELSE); }
<YYINITIAL> "not" 							{ return symbol(ParserSym.NOT); }

<YYINITIAL> {
	/* Identificatori */
	{Name} 									{ return installID(yytext()); }
	 
	/* Letterali */
	{DecIntegerLiteral} 					{ return symbol(ParserSym.INT_CONST, Integer.parseInt(yytext())); }
	{NoIntegerLiterl} 						{ return symbol(ParserSym.DOUBLE_CONST, Double.parseDouble(yytext())); }
	
	/*String*/
	\" 										{ string.setLength(0); yybegin(STRING); }
	
	/* Operatori */
	";" 									{ return symbol(ParserSym.SEMI); }
	"," 									{ return symbol(ParserSym.COMMA); }
	"(" 									{ return symbol(ParserSym.LPAR); }
	")" 									{ return symbol(ParserSym.RPAR); }
	":" 									{ return symbol(ParserSym.COLON); }
	"{" 									{ return symbol(ParserSym.LGPAR); }
	"}" 									{ return symbol(ParserSym.RGPAR); }
	"+" 									{ return symbol(ParserSym.PLUS); }
	"-" 									{ return symbol(ParserSym.MINUS); }
	"*" 									{ return symbol(ParserSym.TIMES); }
	"/" 									{ return symbol(ParserSym.DIV); }
	"=" 									{ return symbol(ParserSym.ASSIGN); }
	">" 									{ return symbol(ParserSym.GT); }
	">=" 									{ return symbol(ParserSym.GE); }
	"<" 									{ return symbol(ParserSym.LT); }
	"<=" 									{ return symbol(ParserSym.LE); }
	"==" 									{ return symbol(ParserSym.EQ); }
	"&&" 									{ return symbol(ParserSym.AND); }
	"||" 									{ return symbol(ParserSym.OR);}
	"<-"									{ return symbol(ParserSym.READ); }
	"->"									{ return symbol(ParserSym.WRITE); }
	
	/* Commenti */
	{Comment}								{ /* ignore */}
	
	/* Spazi bianchi */
	{WhiteSpace}	 						{ /* ignore */ }
}

<STRING> {
	\" 										{ yybegin(YYINITIAL); return symbol(ParserSym.STRING_CONST, string.toString()); }
	[^\n\r\"\\]+ 							{ string.append( yytext() ); }
	\\t 									{ string.append('\t'); }
	\\n 									{ string.append('\n'); }
	\\r 									{ string.append('\r'); }
	\\\" 									{ string.append('\"'); }
	\\ 										{ string.append('\\'); }
}

<<EOF>>										{ return symbol(ParserSym.EOF); }

/* Errori (Stato pozzo) */
[^] 										{ throw new Error("Illegal character <"+ yytext()+">"); }