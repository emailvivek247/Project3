
package com.fdt.sdl.core.analyzer;

import java.io.*;

class JFlexParser {

  /** This character denotes the end of file */
  public static final int YYEOF = -1;

  /** initial size of the lookahead buffer */
  private static final int ZZ_BUFFERSIZE = 16384;

  /** lexical states */
  public static final int YYINITIAL = 0;

  /** 
   * Translates characters to character classes
   */
  private static final String ZZ_CMAP_PACKED = 
    "\11\0\1\0\1\24\1\0\1\0\1\23\22\0\1\0\1\46\4\0"+
    "\1\41\1\30\4\0\1\45\1\32\1\33\1\34\12\26\1\37\1\36"+
    "\1\0\1\42\1\0\1\35\1\40\32\25\4\0\1\31\1\0\1\21"+
    "\1\14\1\16\1\12\1\11\1\5\1\7\1\43\1\2\2\25\1\3"+
    "\1\1\1\4\1\6\1\22\1\25\1\17\1\44\1\20\1\13\1\10"+
    "\3\25\1\15\105\0\27\25\1\0\37\25\1\0\u0568\25\12\27\206\25"+
    "\12\27\u026c\25\12\27\166\25\12\27\166\25\12\27\166\25\12\27\166\25"+
    "\12\27\167\25\11\27\166\25\12\27\166\25\12\27\166\25\12\27\340\25"+
    "\12\27\166\25\12\27\u0166\25\12\27\u0fb6\25\u1040\0\u0150\25\u0170\0\200\25"+
    "\200\0\u092e\25\u10d2\0\u5200\25\u5900\0\u0200\25\u0500\0";

  /** 
   * Translates characters to character classes
   */
  private static final char [] ZZ_CMAP = zzUnpackCMap(ZZ_CMAP_PACKED);

  /** 
   * Translates DFA states to action switch labels.
   */
  private static final int [] ZZ_ACTION = zzUnpackAction();

  private static final String ZZ_ACTION_PACKED_0 =
    "\1\0\1\1\3\2\1\1\2\3\1\4\1\5\3\2"+
    "\5\0\1\2\2\3\4\0\1\2\2\0\2\2\1\6"+
    "\1\7\1\6\1\0\1\10\1\2\1\0\5\3\1\2"+
    "\13\0\1\2\2\0\1\10\2\0\2\3\1\10\1\2"+
    "\12\11\1\12\1\2\1\7\1\0\1\11\1\0\2\6"+
    "\1\2\5\0\1\3\1\13\2\0\1\13\2\0\1\13"+
    "\4\0\1\13\14\0\1\3\15\13\1\0\1\13\2\0"+
    "\1\13";

  private static int [] zzUnpackAction() {
    int [] result = new int[132];
    int offset = 0;
    offset = zzUnpackAction(ZZ_ACTION_PACKED_0, offset, result);
    return result;
  }

  private static int zzUnpackAction(String packed, int offset, int [] result) {
    int i = 0;       /* index in packed string  */
    int j = offset;  /* index in unpacked array */
    int l = packed.length();
    while (i < l) {
      int count = packed.charAt(i++);
      int value = packed.charAt(i++);
      do result[j++] = value; while (--count > 0);
    }
    return j;
  }


  /** 
   * Translates a state to a row index in the transition table
   */
  private static final int [] ZZ_ROWMAP = zzUnpackRowMap();

  private static final String ZZ_ROWMAP_PACKED_0 =
    "\0\0\0\47\0\116\0\165\0\234\0\303\0\352\0\u0111"+
    "\0\47\0\u0138\0\u015f\0\u0186\0\u01ad\0\u01d4\0\u01fb\0\u0222"+
    "\0\u0249\0\u0270\0\u0297\0\u02be\0\u02e5\0\u030c\0\u0333\0\u035a"+
    "\0\u0381\0\u03a8\0\u03cf\0\u03f6\0\u041d\0\u0444\0\u046b\0\u0492"+
    "\0\u04b9\0\u04e0\0\u0507\0\u052e\0\u0555\0\u057c\0\u05a3\0\u05ca"+
    "\0\u05f1\0\u0618\0\u063f\0\u0666\0\u068d\0\u06b4\0\u06db\0\u0702"+
    "\0\u0729\0\u0750\0\u0777\0\u079e\0\u07c5\0\u07ec\0\u0813\0\u083a"+
    "\0\u0861\0\u0249\0\u0888\0\u08af\0\u08d6\0\u08fd\0\u0381\0\u0924"+
    "\0\u094b\0\u0972\0\u0999\0\u09c0\0\u09e7\0\u0a0e\0\u0a35\0\u0a5c"+
    "\0\u0a83\0\u0aaa\0\u0ad1\0\u0af8\0\u0b1f\0\u0b46\0\u0b6d\0\u0b94"+
    "\0\u0bbb\0\u0be2\0\u0c09\0\u0c30\0\u0c57\0\u0c7e\0\u0ca5\0\u0ccc"+
    "\0\u0cf3\0\u0d1a\0\u0d41\0\u0d68\0\u0d8f\0\u0db6\0\u0ddd\0\u0e04"+
    "\0\u0e2b\0\u0e52\0\u0e79\0\u0ea0\0\u0ec7\0\u0eee\0\u0f15\0\u0f3c"+
    "\0\u0f63\0\u0f8a\0\u0fb1\0\u0fd8\0\u0fff\0\u1026\0\u104d\0\u1074"+
    "\0\u109b\0\u10c2\0\u10e9\0\u1110\0\u1137\0\u115e\0\u1185\0\u11ac"+
    "\0\u11d3\0\u11fa\0\u1221\0\u1248\0\u126f\0\u1296\0\u12bd\0\u12e4"+
    "\0\u130b\0\u1332\0\u1359\0\u1380";

  private static int [] zzUnpackRowMap() {
    int [] result = new int[132];
    int offset = 0;
    offset = zzUnpackRowMap(ZZ_ROWMAP_PACKED_0, offset, result);
    return result;
  }

  private static int zzUnpackRowMap(String packed, int offset, int [] result) {
    int i = 0;  /* index in packed string  */
    int j = offset;  /* index in unpacked array */
    int l = packed.length();
    while (i < l) {
      int high = packed.charAt(i++) << 16;
      result[j++] = high | packed.charAt(i++);
    }
    return j;
  }

  /** 
   * The transition table of the DFA
   */
  private static final int [] ZZ_TRANS = zzUnpackTrans();

  private static final String ZZ_TRANS_PACKED_0 =
    "\1\2\1\3\3\4\1\5\15\4\1\6\1\2\1\4"+
    "\1\7\1\10\1\11\1\2\1\11\1\12\1\2\2\12"+
    "\1\11\3\2\1\13\1\4\1\11\1\12\50\0\20\14"+
    "\1\15\1\14\2\0\3\14\1\16\1\17\1\20\1\21"+
    "\5\0\1\22\1\0\2\14\3\0\22\14\2\0\3\14"+
    "\1\16\1\17\1\20\1\21\5\0\1\22\1\0\2\14"+
    "\3\0\17\14\1\23\2\14\2\0\3\14\1\16\1\17"+
    "\1\20\1\21\5\0\1\22\1\0\2\14\26\0\1\2"+
    "\23\0\22\14\2\0\1\14\1\24\1\25\1\0\1\17"+
    "\1\26\2\27\2\0\1\27\3\0\2\14\1\27\2\0"+
    "\22\14\2\0\1\14\2\25\1\16\1\17\1\30\1\31"+
    "\1\27\2\0\1\27\1\0\1\22\1\0\2\14\1\27"+
    "\34\0\1\12\1\0\2\12\7\0\1\12\1\0\17\14"+
    "\1\32\2\14\2\0\3\14\1\16\1\17\1\20\1\21"+
    "\5\0\1\22\1\0\2\14\3\0\22\14\2\0\3\14"+
    "\1\16\1\17\1\20\1\33\4\0\1\34\1\22\1\0"+
    "\2\14\3\0\1\14\1\35\20\14\2\0\3\14\1\16"+
    "\1\17\1\20\1\33\4\0\1\34\1\22\1\0\2\14"+
    "\3\0\22\36\2\0\1\36\1\0\1\36\13\0\2\36"+
    "\3\0\22\37\2\0\3\37\1\0\2\17\10\0\2\37"+
    "\3\0\22\40\2\0\1\40\1\41\1\40\1\0\2\17"+
    "\10\0\2\40\3\0\22\42\2\0\1\42\1\0\1\42"+
    "\13\0\2\42\3\0\22\43\2\0\1\43\1\22\1\43"+
    "\13\0\2\43\3\0\21\14\1\44\2\0\3\14\1\16"+
    "\1\17\1\20\1\33\4\0\1\34\1\22\1\0\2\14"+
    "\3\0\22\14\2\0\1\14\1\24\1\25\1\0\1\17"+
    "\1\26\1\45\1\27\2\0\1\27\1\34\2\0\2\14"+
    "\1\27\2\0\22\14\2\0\1\14\2\25\1\16\1\17"+
    "\1\30\1\45\1\27\2\0\1\27\1\34\1\22\1\0"+
    "\2\14\1\27\2\0\22\37\2\0\1\37\2\46\1\0"+
    "\2\17\10\0\2\37\30\0\2\47\20\0\22\40\2\0"+
    "\1\40\1\50\1\51\1\0\2\17\10\0\2\40\3\0"+
    "\22\42\2\0\1\42\1\47\1\52\13\0\2\42\3\0"+
    "\17\14\1\53\2\14\2\0\3\14\1\16\1\17\1\20"+
    "\1\33\4\0\1\34\1\22\1\0\2\14\3\0\1\54"+
    "\1\55\1\56\1\57\1\56\1\60\1\61\1\56\1\62"+
    "\2\56\1\63\1\56\1\64\2\56\1\65\1\56\2\0"+
    "\1\56\1\17\1\56\13\0\2\56\3\0\22\66\2\0"+
    "\3\66\13\0\2\66\3\0\2\14\1\67\17\14\2\0"+
    "\3\14\1\16\1\17\1\20\1\33\4\0\1\34\1\22"+
    "\1\0\2\14\3\0\22\36\2\0\3\36\1\16\1\0"+
    "\1\70\6\0\1\22\1\0\2\36\3\0\22\37\2\0"+
    "\3\37\1\0\2\17\1\33\4\0\1\34\2\0\2\37"+
    "\3\0\22\40\2\0\3\40\1\71\1\17\1\20\1\33"+
    "\4\0\1\34\2\0\2\40\3\0\22\40\2\0\1\40"+
    "\1\41\1\40\1\0\2\17\1\33\4\0\1\34\2\0"+
    "\2\40\35\0\1\72\14\0\22\43\2\0\3\43\1\73"+
    "\10\0\1\22\1\0\2\43\3\0\22\14\2\0\3\14"+
    "\1\16\1\17\1\20\1\33\3\0\1\74\1\34\1\22"+
    "\1\0\2\14\3\0\1\54\1\55\1\56\1\57\1\56"+
    "\1\60\1\61\1\56\1\62\2\56\1\63\1\56\1\64"+
    "\2\56\1\65\1\56\2\0\1\56\1\75\1\76\13\0"+
    "\2\56\3\0\22\37\2\0\1\37\2\46\1\0\1\17"+
    "\1\26\1\45\1\27\2\0\1\27\1\34\2\0\2\37"+
    "\1\27\27\0\2\47\2\0\3\27\2\0\1\27\5\0"+
    "\1\27\2\0\22\40\2\0\1\40\1\50\1\51\1\0"+
    "\1\17\1\26\1\45\1\27\2\0\1\27\1\34\2\0"+
    "\2\40\1\27\2\0\22\40\2\0\1\40\2\51\1\71"+
    "\1\17\1\30\1\45\1\27\2\0\1\27\1\34\2\0"+
    "\2\40\1\27\27\0\2\47\2\0\1\27\1\77\1\27"+
    "\2\0\1\27\5\0\1\27\2\0\21\14\1\100\2\0"+
    "\3\14\1\16\1\17\1\20\1\33\4\0\1\34\1\22"+
    "\1\0\2\14\3\0\1\101\1\102\20\101\2\0\1\101"+
    "\1\37\1\101\1\0\2\17\10\0\2\101\3\0\3\101"+
    "\1\103\16\101\2\0\1\101\1\37\1\101\1\0\2\17"+
    "\10\0\2\101\3\0\22\101\2\0\1\101\1\37\1\101"+
    "\1\0\2\17\10\0\2\101\3\0\10\101\1\104\11\101"+
    "\2\0\1\101\1\37\1\101\1\0\2\17\10\0\2\101"+
    "\3\0\16\101\1\105\3\101\2\0\1\101\1\37\1\101"+
    "\1\0\2\17\10\0\2\101\3\0\5\101\1\106\14\101"+
    "\2\0\1\101\1\37\1\101\1\0\2\17\10\0\2\101"+
    "\3\0\11\101\1\107\10\101\2\0\1\101\1\37\1\101"+
    "\1\0\2\17\10\0\2\101\3\0\1\101\1\110\20\101"+
    "\2\0\1\101\1\37\1\101\1\0\2\17\10\0\2\101"+
    "\3\0\5\101\1\111\14\101\2\0\1\101\1\37\1\101"+
    "\1\0\2\17\10\0\2\101\3\0\16\101\1\112\3\101"+
    "\2\0\1\101\1\37\1\101\1\0\2\17\10\0\2\101"+
    "\3\0\22\113\2\0\3\113\1\0\2\66\10\0\2\113"+
    "\3\0\17\14\1\114\2\14\2\0\3\14\1\16\1\17"+
    "\1\20\1\33\4\0\1\34\1\22\1\0\2\14\3\0"+
    "\22\115\2\0\1\115\1\70\1\115\13\0\2\115\3\0"+
    "\22\115\2\0\1\115\1\0\1\115\13\0\2\115\3\0"+
    "\22\43\2\0\1\43\1\0\1\43\13\0\2\43\36\0"+
    "\1\116\13\0\22\37\2\0\1\37\2\46\1\0\1\17"+
    "\1\26\2\27\2\0\1\27\3\0\2\37\1\27\2\0"+
    "\22\101\2\0\1\101\1\46\1\117\1\0\1\17\1\26"+
    "\2\27\2\0\1\27\3\0\2\101\1\27\2\0\22\14"+
    "\2\0\3\14\1\16\1\17\1\20\1\33\3\0\1\74"+
    "\1\34\1\22\1\0\1\14\1\44\3\0\22\37\2\0"+
    "\3\37\1\0\2\17\1\33\1\120\3\0\1\34\2\0"+
    "\2\37\3\0\2\37\1\101\17\37\2\0\3\37\1\0"+
    "\2\17\1\33\1\120\3\0\1\34\2\0\2\37\3\0"+
    "\4\37\1\121\15\37\2\0\3\37\1\0\2\17\1\33"+
    "\1\120\3\0\1\34\2\0\2\37\3\0\17\37\1\101"+
    "\2\37\2\0\3\37\1\0\2\17\1\33\1\120\3\0"+
    "\1\34\2\0\2\37\3\0\6\37\1\101\13\37\2\0"+
    "\3\37\1\0\2\17\1\33\1\120\3\0\1\34\2\0"+
    "\2\37\3\0\7\37\1\101\12\37\2\0\3\37\1\0"+
    "\2\17\1\33\1\120\3\0\1\34\2\0\2\37\3\0"+
    "\12\37\1\101\7\37\2\0\3\37\1\0\2\17\1\33"+
    "\1\120\3\0\1\34\2\0\2\37\3\0\14\37\1\101"+
    "\5\37\2\0\3\37\1\0\2\17\1\33\1\120\3\0"+
    "\1\34\2\0\2\37\3\0\1\101\21\37\2\0\3\37"+
    "\1\0\2\17\1\33\1\120\3\0\1\34\2\0\2\37"+
    "\3\0\21\37\1\122\2\0\3\37\1\0\2\17\1\33"+
    "\1\120\3\0\1\34\2\0\2\37\3\0\22\113\2\0"+
    "\3\113\1\0\2\66\1\34\7\0\2\113\3\0\5\14"+
    "\1\123\14\14\2\0\3\14\1\16\1\17\1\20\1\33"+
    "\4\0\1\34\1\22\1\0\2\14\3\0\22\115\2\0"+
    "\3\115\1\71\1\0\1\70\10\0\2\115\36\0\1\124"+
    "\13\0\22\37\2\0\1\37\2\46\1\0\1\17\1\26"+
    "\1\45\1\125\2\0\1\27\1\34\2\0\2\37\1\27"+
    "\2\0\22\126\2\0\3\126\13\0\2\126\3\0\5\37"+
    "\1\101\14\37\2\0\3\37\1\0\2\17\1\33\4\0"+
    "\1\34\2\0\2\37\3\0\20\37\1\101\1\37\2\0"+
    "\3\37\1\0\2\17\1\33\4\0\1\34\2\0\2\37"+
    "\3\0\22\14\2\0\3\14\1\16\1\17\1\20\1\33"+
    "\3\0\1\127\1\34\1\22\1\0\2\14\3\0\22\130"+
    "\2\0\3\130\13\0\2\130\3\0\22\126\2\0\1\126"+
    "\2\131\13\0\2\126\3\0\22\132\2\0\3\132\1\0"+
    "\2\126\10\0\2\132\3\0\22\133\2\0\3\133\13\0"+
    "\2\133\3\0\22\134\2\0\3\134\1\0\2\130\10\0"+
    "\2\134\3\0\22\132\2\0\1\132\2\135\1\0\1\126"+
    "\1\136\2\27\2\0\1\27\3\0\2\132\1\27\2\0"+
    "\22\132\2\0\3\132\1\0\2\126\1\137\1\140\1\141"+
    "\5\0\2\132\3\0\22\142\2\0\3\142\1\0\2\133"+
    "\10\0\2\142\3\0\22\134\2\0\3\134\1\0\2\130"+
    "\1\143\7\0\2\134\3\0\22\132\2\0\1\132\2\135"+
    "\1\0\1\126\1\136\1\144\1\145\1\141\1\0\1\27"+
    "\3\0\2\132\1\27\2\0\22\132\2\0\1\132\2\135"+
    "\1\0\2\126\10\0\2\132\3\0\22\146\2\0\3\146"+
    "\13\0\2\146\3\0\22\126\2\0\3\126\5\0\1\141"+
    "\5\0\2\126\3\0\22\147\2\0\3\147\13\0\2\147"+
    "\3\0\22\142\2\0\3\142\1\0\2\133\1\127\4\0"+
    "\1\34\2\0\2\142\3\0\1\150\1\151\1\152\1\153"+
    "\1\152\1\154\1\155\1\152\1\156\2\152\1\157\1\152"+
    "\1\160\2\152\1\161\1\152\2\0\1\152\1\130\1\152"+
    "\13\0\2\152\3\0\22\146\2\0\1\146\2\162\13\0"+
    "\2\146\3\0\22\126\2\0\1\126\2\131\5\0\1\141"+
    "\5\0\2\126\3\0\22\163\2\0\3\163\1\0\2\146"+
    "\10\0\2\163\3\0\22\164\2\0\3\164\1\0\2\147"+
    "\10\0\2\164\3\0\1\165\1\166\20\165\2\0\1\165"+
    "\1\134\1\165\1\0\2\130\10\0\2\165\3\0\3\165"+
    "\1\167\16\165\2\0\1\165\1\134\1\165\1\0\2\130"+
    "\10\0\2\165\3\0\22\165\2\0\1\165\1\134\1\165"+
    "\1\0\2\130\10\0\2\165\3\0\10\165\1\170\11\165"+
    "\2\0\1\165\1\134\1\165\1\0\2\130\10\0\2\165"+
    "\3\0\16\165\1\171\3\165\2\0\1\165\1\134\1\165"+
    "\1\0\2\130\10\0\2\165\3\0\5\165\1\172\14\165"+
    "\2\0\1\165\1\134\1\165\1\0\2\130\10\0\2\165"+
    "\3\0\11\165\1\173\10\165\2\0\1\165\1\134\1\165"+
    "\1\0\2\130\10\0\2\165\3\0\1\165\1\174\20\165"+
    "\2\0\1\165\1\134\1\165\1\0\2\130\10\0\2\165"+
    "\3\0\5\165\1\175\14\165\2\0\1\165\1\134\1\165"+
    "\1\0\2\130\10\0\2\165\3\0\16\165\1\176\3\165"+
    "\2\0\1\165\1\134\1\165\1\0\2\130\10\0\2\165"+
    "\3\0\22\163\2\0\1\163\2\177\1\0\1\146\1\200"+
    "\2\27\2\0\1\27\3\0\2\163\1\27\2\0\22\163"+
    "\2\0\3\163\1\0\2\146\1\0\1\201\1\141\5\0"+
    "\2\163\3\0\22\164\2\0\3\164\1\0\2\147\3\0"+
    "\5\141\2\164\3\0\22\134\2\0\3\134\1\0\2\130"+
    "\1\143\1\120\6\0\2\134\3\0\2\134\1\165\17\134"+
    "\2\0\3\134\1\0\2\130\1\143\1\120\6\0\2\134"+
    "\3\0\4\134\1\202\15\134\2\0\3\134\1\0\2\130"+
    "\1\143\1\120\6\0\2\134\3\0\17\134\1\165\2\134"+
    "\2\0\3\134\1\0\2\130\1\143\1\120\6\0\2\134"+
    "\3\0\6\134\1\165\13\134\2\0\3\134\1\0\2\130"+
    "\1\143\1\120\6\0\2\134\3\0\7\134\1\165\12\134"+
    "\2\0\3\134\1\0\2\130\1\143\1\120\6\0\2\134"+
    "\3\0\12\134\1\165\7\134\2\0\3\134\1\0\2\130"+
    "\1\143\1\120\6\0\2\134\3\0\14\134\1\165\5\134"+
    "\2\0\3\134\1\0\2\130\1\143\1\120\6\0\2\134"+
    "\3\0\1\165\21\134\2\0\3\134\1\0\2\130\1\143"+
    "\1\120\6\0\2\134\3\0\21\134\1\203\2\0\3\134"+
    "\1\0\2\130\1\143\1\120\6\0\2\134\3\0\22\163"+
    "\2\0\1\163\2\177\1\0\1\146\1\200\1\27\1\204"+
    "\1\141\1\0\1\27\3\0\2\163\1\27\2\0\22\163"+
    "\2\0\1\163\2\177\1\0\2\146\10\0\2\163\37\0"+
    "\1\141\12\0\5\134\1\165\14\134\2\0\3\134\1\0"+
    "\2\130\1\143\7\0\2\134\3\0\20\134\1\165\1\134"+
    "\2\0\3\134\1\0\2\130\1\143\7\0\2\134\30\0"+
    "\2\47\5\0\1\141\11\0";

  private static int [] zzUnpackTrans() {
    int [] result = new int[5031];
    int offset = 0;
    offset = zzUnpackTrans(ZZ_TRANS_PACKED_0, offset, result);
    return result;
  }

  private static int zzUnpackTrans(String packed, int offset, int [] result) {
    int i = 0;       /* index in packed string  */
    int j = offset;  /* index in unpacked array */
    int l = packed.length();
    while (i < l) {
      int count = packed.charAt(i++);
      int value = packed.charAt(i++);
      value--;
      do result[j++] = value; while (--count > 0);
    }
    return j;
  }


  /* error codes */
  private static final int ZZ_UNKNOWN_ERROR = 0;
  private static final int ZZ_NO_MATCH = 1;
  private static final int ZZ_PUSHBACK_2BIG = 2;

  /* error messages for the codes above */
  private static final String ZZ_ERROR_MSG[] = {
    "Unkown internal scanner error",
    "Error: could not match input",
    "Error: pushback value was too large"
  };

  /**
   * ZZ_ATTRIBUTE[aState] contains the attributes of state <code>aState</code>
   */
  private static final int [] ZZ_ATTRIBUTE = zzUnpackAttribute();

  private static final String ZZ_ATTRIBUTE_PACKED_0 =
    "\1\0\1\11\6\1\1\11\4\1\5\0\3\1\4\0"+
    "\1\1\2\0\5\1\1\0\2\1\1\0\6\1\13\0"+
    "\1\1\2\0\1\1\2\0\21\1\1\0\1\1\1\0"+
    "\3\1\5\0\2\1\2\0\1\1\2\0\1\1\4\0"+
    "\1\1\14\0\16\1\1\0\1\1\2\0\1\1";

  private static int [] zzUnpackAttribute() {
    int [] result = new int[132];
    int offset = 0;
    offset = zzUnpackAttribute(ZZ_ATTRIBUTE_PACKED_0, offset, result);
    return result;
  }

  private static int zzUnpackAttribute(String packed, int offset, int [] result) {
    int i = 0;       /* index in packed string  */
    int j = offset;  /* index in unpacked array */
    int l = packed.length();
    while (i < l) {
      int count = packed.charAt(i++);
      int value = packed.charAt(i++);
      do result[j++] = value; while (--count > 0);
    }
    return j;
  }

  /** the input device */
  private java.io.Reader zzReader;

  /** the current state of the DFA */
  private int zzState;

  /** the current lexical state */
  private int zzLexicalState = YYINITIAL;

  /** this buffer contains the current text to be matched and is
      the source of the yytext() string */
  private char zzBuffer[] = new char[ZZ_BUFFERSIZE];

  /** the textposition at the last accepting state */
  private int zzMarkedPos;

  /** the textposition at the last state to be included in yytext */
  private int zzPushbackPos;

  /** the current text position in the buffer */
  private int zzCurrentPos;

  /** startRead marks the beginning of the yytext() string in the buffer */
  private int zzStartRead;

  /** endRead marks the last character in the buffer, that has been read
      from input */
  private int zzEndRead;

  /** number of newlines encountered up to the start of the matched text */
  private int yyline;

  /** the number of characters up to the start of the matched text */
  private int yychar;

  /**
   * the number of characters from the last newline up to the start of the 
   * matched text
   */
  private int yycolumn;

  /** 
   * zzAtBOL == true <=> the scanner is currently at the beginning of a line
   */
  private boolean zzAtBOL = true;

  /** zzAtEOF == true <=> the scanner is at the EOF */
  private boolean zzAtEOF;

  /* user code: */
public static final int TERM           = 1;
public static final int NUMERIC        = 2;
public static final int SENTENCEMARKER = 3;
public static final int PUNCTUATION    = 4;
public static final int EMAIL          = 5;
public static final int ACRONYM        = 6;
public static final int FULL_URL       = 7;
public static final int BARE_URL       = 8;
public static final int FILE           = 9;
public static final int HYPHTERM       = 10;


  /**
   * Creates a new scanner
   * There is also a java.io.InputStream version of this constructor.
   *
   * @param   in  the java.io.Reader to read input from.
   */
JFlexParser(java.io.Reader in) {
    this.zzReader = in;
  }

  /**
   * Creates a new scanner.
   * There is also java.io.Reader version of this constructor.
   *
   * @param   in  the java.io.Inputstream to read input from.
   */
JFlexParser(java.io.InputStream in) {
    this(new java.io.InputStreamReader(in));
  }

  /** 
   * Unpacks the compressed character translation table.
   *
   * @param packed   the packed character translation table
   * @return         the unpacked character translation table
   */
  private static char [] zzUnpackCMap(String packed) {
    char [] map = new char[0x10000];
    int i = 0;  /* index in packed string  */
    int j = 0;  /* index in unpacked array */
    while (i < 194) {
      int  count = packed.charAt(i++);
      char value = packed.charAt(i++);
      do map[j++] = value; while (--count > 0);
    }
    return map;
  }


  /**
   * Refills the input buffer.
   *
   * @return      <code>false</code>, iff there was new input.
   * 
   * @exception   java.io.IOException  if any I/O-Error occurs
   */
  private boolean zzRefill() throws java.io.IOException {

    /* first: make room (if you can) */
    if (zzStartRead > 0) {
      System.arraycopy(zzBuffer, zzStartRead,
                       zzBuffer, 0,
                       zzEndRead-zzStartRead);

      /* translate stored positions */
      zzEndRead-= zzStartRead;
      zzCurrentPos-= zzStartRead;
      zzMarkedPos-= zzStartRead;
      zzPushbackPos-= zzStartRead;
      zzStartRead = 0;
    }

    /* is the buffer big enough? */
    if (zzCurrentPos >= zzBuffer.length) {
      /* if not: blow it up */
      char newBuffer[] = new char[zzCurrentPos*2];
      System.arraycopy(zzBuffer, 0, newBuffer, 0, zzBuffer.length);
      zzBuffer = newBuffer;
    }

    /* finally: fill the buffer with new input */
    int numRead = zzReader.read(zzBuffer, zzEndRead,
                                            zzBuffer.length-zzEndRead);

    if (numRead < 0) {
      return true;
    }
    else {
      zzEndRead+= numRead;
      return false;
    }
  }

    
  /**
   * Closes the input stream.
   */
  public final void yyclose() throws java.io.IOException {
    zzAtEOF = true;            /* indicate end of file */
    zzEndRead = zzStartRead;  /* invalidate buffer    */

    if (zzReader != null)
      zzReader.close();
  }


  /**
   * Resets the scanner to read from a new input stream.
   * Does not close the old reader.
   *
   * All internal variables are reset, the old input stream 
   * <b>cannot</b> be reused (internal buffer is discarded and lost).
   * Lexical state is set to <tt>ZZ_INITIAL</tt>.
   *
   * @param reader   the new input stream 
   */
  public final void yyreset(java.io.Reader reader) {
    zzReader = reader;
    zzAtBOL  = true;
    zzAtEOF  = false;
    zzEndRead = zzStartRead = 0;
    zzCurrentPos = zzMarkedPos = zzPushbackPos = 0;
    yyline = yychar = yycolumn = 0;
    zzLexicalState = YYINITIAL;
  }


  /**
   * Returns the current lexical state.
   */
  public final int yystate() {
    return zzLexicalState;
  }


  /**
   * Enters a new lexical state
   *
   * @param newState the new lexical state
   */
  public final void yybegin(int newState) {
    zzLexicalState = newState;
  }


  /**
   * Returns the text matched by the current regular expression.
   */
  public final String yytext() {
    return new String( zzBuffer, zzStartRead, zzMarkedPos-zzStartRead );
  }


  /**
   * Returns the character at position <tt>pos</tt> from the 
   * matched text. 
   * 
   * It is equivalent to yytext().charAt(pos), but faster
   *
   * @param pos the position of the character to fetch. 
   *            A value from 0 to yylength()-1.
   *
   * @return the character at position pos
   */
  public final char yycharat(int pos) {
    return zzBuffer[zzStartRead+pos];
  }


  /**
   * Returns the length of the matched text region.
   */
  public final int yylength() {
    return zzMarkedPos-zzStartRead;
  }

  public int yyposition(){
      if (zzStartRead ==0 )return -zzPushbackPos;
      return zzStartRead;
  }

  /**
   * Reports an error that occured while scanning.
   *
   * In a wellformed scanner (no or only correct usage of 
   * yypushback(int) and a match-all fallback rule) this method 
   * will only be called with things that "Can't Possibly Happen".
   * If this method is called, something is seriously wrong
   * (e.g. a JFlex bug producing a faulty scanner etc.).
   *
   * Usual syntax/scanner level error handling should be done
   * in error fallback rules.
   *
   * @param   errorCode  the code of the errormessage to display
   */
  private void zzScanError(int errorCode) {
    String message;
    try {
      message = ZZ_ERROR_MSG[errorCode];
    }
    catch (ArrayIndexOutOfBoundsException e) {
      message = ZZ_ERROR_MSG[ZZ_UNKNOWN_ERROR];
    }

    throw new Error(message);
  } 


  /**
   * Pushes the specified amount of characters back into the input stream.
   *
   * They will be read again by then next call of the scanning method
   *
   * @param number  the number of characters to be read again.
   *                This number must not be greater than yylength()!
   */
  public void yypushback(int number)  {
    if ( number > yylength() )
      zzScanError(ZZ_PUSHBACK_2BIG);

    zzMarkedPos -= number;
  }


  /**
   * Resumes scanning until the next regular expression is matched,
   * the end of input is encountered or an I/O-Error occurs.
   *
   * @return      the next token
   * @exception   java.io.IOException  if any I/O-Error occurs
   */
  public int getNextToken() throws java.io.IOException {
    int zzInput;
    int zzAction;

    // cached fields:
    int zzCurrentPosL;
    int zzMarkedPosL;
    int zzEndReadL = zzEndRead;
    char [] zzBufferL = zzBuffer;
    char [] zzCMapL = ZZ_CMAP;

    int [] zzTransL = ZZ_TRANS;
    int [] zzRowMapL = ZZ_ROWMAP;
    int [] zzAttrL = ZZ_ATTRIBUTE;

    while (true) {
      zzMarkedPosL = zzMarkedPos;

      zzAction = -1;

      zzCurrentPosL = zzCurrentPos = zzStartRead = zzMarkedPosL;
  
      zzState = zzLexicalState;


      zzForAction: {
        while (true) {
    
          if (zzCurrentPosL < zzEndReadL)
            zzInput = zzBufferL[zzCurrentPosL++];
          else if (zzAtEOF) {
            zzInput = YYEOF;
            break zzForAction;
          }
          else {
            // store back cached positions
            zzCurrentPos  = zzCurrentPosL;
            zzMarkedPos   = zzMarkedPosL;
            boolean eof = zzRefill();
            // get translated positions and possibly new buffer
            zzCurrentPosL  = zzCurrentPos;
            zzMarkedPosL   = zzMarkedPos;
            zzBufferL      = zzBuffer;
            zzEndReadL     = zzEndRead;
            if (eof) {
              zzInput = YYEOF;
              break zzForAction;
            }
            else {
              zzInput = zzBufferL[zzCurrentPosL++];
            }
          }
          int zzNext = zzTransL[ zzRowMapL[zzState] + zzCMapL[zzInput] ];
          if (zzNext == -1) break zzForAction;
          zzState = zzNext;

          int zzAttributes = zzAttrL[zzState];
          if ( (zzAttributes & 1) == 1 ) {
            zzAction = zzState;
            zzMarkedPosL = zzCurrentPosL;
            if ( (zzAttributes & 8) == 8 ) break zzForAction;
          }

        }
      }

      // store back cached position
      zzMarkedPos = zzMarkedPosL;

      switch (zzAction < 0 ? zzAction : ZZ_ACTION[zzAction]) {
        case 8: 
          { return ACRONYM;
          }
        case 12: break;
        case 11: 
          { return FULL_URL;
          }
        case 13: break;
        case 4: 
          { return PUNCTUATION;
          }
        case 14: break;
        case 1: 
          { ;
          }
        case 15: break;
        case 9: 
          { return BARE_URL;
          }
        case 16: break;
        case 6: 
          { return FILE;
          }
        case 17: break;
        case 7: 
          { return HYPHTERM;
          }
        case 18: break;
        case 2: 
          { return TERM;
          }
        case 19: break;
        case 10: 
          { return EMAIL;
          }
        case 20: break;
        case 3: 
          { return NUMERIC;
          }
        case 21: break;
        case 5: 
          { return SENTENCEMARKER;
          }
        case 22: break;
        default: 
          if (zzInput == YYEOF && zzStartRead == zzCurrentPos) {
            zzAtEOF = true;
            return YYEOF;
          } 
          else {
            zzScanError(ZZ_NO_MATCH);
          }
      }
    }
  }
public static void main (String[] arg){
    JFlexParser jflex= new JFlexParser((Reader)new StringReader("1 2 3/34 234 test"));
    try {
        int t;
    while( (t=jflex.getNextToken())!= YYEOF)
   System.out.println("pos="+ jflex.yyposition()+";text="+ jflex.yytext());
    }catch (Exception e){
        e.printStackTrace();
    }
}

}
