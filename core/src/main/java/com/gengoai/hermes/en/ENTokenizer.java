/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package com.gengoai.hermes.en;

import com.gengoai.collection.Iterables;
import com.gengoai.hermes.lexicon.GlobalLexica;
import com.gengoai.hermes.lexicon.TrieWordList;
import com.gengoai.hermes.lexicon.WordList;
import com.gengoai.hermes.morphology.StandardTokenizer;
import com.gengoai.hermes.morphology.TokenType;
import com.gengoai.hermes.morphology.Tokenizer;
import com.gengoai.string.Strings;
import lombok.NonNull;

import java.io.IOException;
import java.io.Reader;
import java.io.Serializable;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.NoSuchElementException;
import java.util.Set;

/**
 * The type English tokenizer.
 *
 * @author David B. Bracewell
 */
public class ENTokenizer implements Tokenizer, Serializable {
   private static final long serialVersionUID = 1L;
   private final TrieWordList abbreviations;
   private final TrieWordList emoticons;
   private final WordList tlds;

   /**
    * Instantiates a new English tokenizer.
    */
   public ENTokenizer() {
      this.abbreviations = GlobalLexica.getAbbreviations();
      this.tlds = GlobalLexica.getTopLevelDomains();
      this.emoticons = GlobalLexica.getEmoticons();
   }

   @Override
   public Iterable<Token> tokenize(@NonNull Reader reader) {
      return Iterables.asIterable(new TokenIterator(reader));
   }

   private class TokenIterator implements Iterator<Token> {
      private final LinkedList<Token> buffer = new LinkedList<>();
      private final StandardTokenizer tokenizer;
      private int lastIndex = 0;

      private TokenIterator(Reader reader) {
         this.tokenizer = new StandardTokenizer(reader);
      }

      private void addToBuffer(Token token) {
         if (token != null && !Strings.isNullOrBlank(token.text)) {
            buffer.addFirst(token);
         }
      }

      private Token checkURL(Token n) {
         //Ensure that the TLD is valid

//         System.err.println(n);
         if (n.text.contains("://")) {
            return n;
         }
         int slash = n.text.indexOf('/');
         if (slash == -1) {
            slash = n.text.length();
         }
         int dot = n.text.substring(0, slash).lastIndexOf('.');
         String tld = n.text.substring(dot + 1, slash);

         if (!tlds.contains(tld.toLowerCase())) {
            Token nn = peek(0);
            if (nn != null && nn.charStartIndex == n.charEndIndex) {
               consume();
               addToBuffer(new Token( //Add the bad tld
                                      tld + nn.text,
                                      TokenType.ALPHA_NUMERIC,
                                      n.charStartIndex + dot + 1,
                                      nn.charEndIndex,
                                      n.index
                           )
                          );
            } else {
               addToBuffer(new Token( //Add the bad tld
                                      tld,
                                      TokenType.ALPHA_NUMERIC,
                                      n.charStartIndex + dot + 1,
                                      n.charEndIndex,
                                      n.index
                           )
                          );
            }

            addToBuffer(new Token( //Add the dot to the buffer
                                   n.text.substring(dot, dot + 1),
                                   TokenType.PUNCTUATION,
                                   n.charStartIndex + dot,
                                   n.charStartIndex + dot + 1,
                                   n.index
                        )
                       );

            n = new Token( //Change the token to the first part of the bad url
                           n.text.substring(0, dot),
                           TokenType.ALPHA_NUMERIC,
                           n.charStartIndex,
                           n.charStartIndex + dot,
                           n.index
            );

         }
         return n;
      }

      private Token consume(int number) {
         Token token = null;
         while (number >= 0) {
            token = consume();
            number--;
         }
         return token;
      }

      private Token consume() {
         peek(0);
         while (!buffer.isEmpty()) {
            Token token = buffer.remove();
            if (token != null && !Strings.isNullOrBlank(token.text)) {
               return token;
            }
         }
         return null;
      }

      private Token handleEmoticon(Token n) {
         String emo = n.text;
         String emoLower = n.text.toLowerCase();
         if (!emoticons.isPrefixMatch(emoLower)) {
            return n;
         }

         Token nn;
         Token last = n;
         int end = n.charEndIndex;
         int peek = 0;
         while ((nn = peek(peek)) != null) {
            String tempLower = emoLower;
            if (last.charEndIndex < nn.charStartIndex) {
               tempLower += Strings.repeat(' ', nn.charStartIndex - last.charEndIndex);
            }
            tempLower += nn.text.toLowerCase();
            last = nn;
            Set<String> prefixes = emoticons.prefixes(tempLower);
            if (emoticons.prefixes(tempLower).size() > 1 || (prefixes.size() == 1 && !prefixes.contains(tempLower))) {
               end = nn.charEndIndex;
               emo = emo + nn.text;
               emoLower = tempLower;
               peek++;
            } else if (emoticons.contains(tempLower)) {
               consume(peek);
               lastIndex = n.index;
               return new Token(emo, TokenType.EMOTICON, n.charStartIndex, nn.charEndIndex, n.index);
            } else if (emoticons.contains(emoLower)) {
               last = consume(peek - 1);
               lastIndex = n.index;
               return new Token(emo, TokenType.EMOTICON, n.charStartIndex, last.charEndIndex, n.index);
            } else {
               return n;
            }
         }

         if (emoticons.contains(emoLower)) {
            if (emoLower.length() > 1) {
               nn = consume(peek - 1);
               return new Token(emo, TokenType.EMOTICON, n.charStartIndex, nn.charEndIndex, n.index);
            } else {
               return new Token(emo, TokenType.EMOTICON, n.charStartIndex, n.charEndIndex, n.index);
            }
         }

         return n;
      }

      @Override
      public boolean hasNext() {
         return peek(0) != null;
      }

      private Token mergeAbbreviationAndAcronym(Token n) {
         String abbreviation = n.text;
         int end = n.charEndIndex;
         int peek = 0;
         Token pn = n;
         Token nn;
         while ((nn = peek(peek)) != null) {
            String temp = abbreviation;
            if (pn.charEndIndex < nn.charStartIndex) {
               temp += Strings.repeat(' ', nn.charStartIndex - pn.charEndIndex);
            }
            temp += nn.text;
            if (nn.charStartIndex == pn.charEndIndex && abbreviations.contains(temp)) {
               peek++;
               end = nn.charEndIndex;
               abbreviation = temp;
            } else if (peek == 0) {
               if (abbreviations.contains(n.text.toLowerCase())) {
                  return new Token(n.text,
                                   TokenType.ACRONYM,
                                   n.charStartIndex,
                                   n.charEndIndex,
                                   n.index);
               }
               return n;
            } else {
               consume(peek - 1);
               return new Token(abbreviation, TokenType.ACRONYM, n.charStartIndex, end, n.index);
            }
            pn = nn;
         }
         return n;
      }

      private Token mergeMoneyNumber(Token n) {
         Token nn = peek(0);
         if (nn == null) {
            return n;
         }
         if (nn.type.isInstance(TokenType.NUMBER) && nn.charStartIndex == n.charEndIndex) {
            Token token = new Token(
               n.text + nn.text,
               TokenType.MONEY,
               n.charStartIndex,
               nn.charEndIndex,
               n.index
            );
            consume();
            return token;
         }
         return n;
      }

      private Token mergeMultiHyphens(Token n) {
         String text = n.text;
         int end = n.charEndIndex;
         while (peekIsType(0, TokenType.HYPHEN)) {
            Token nn = consume();
            end = nn.charEndIndex;
            text += nn.text;
         }
         if (end != n.charEndIndex) {
            return new Token(text, TokenType.HYPHEN, n.charStartIndex, end, 0);
         }
         return n;
      }

      @Override
      public Token next() {
         if (peek(0) == null) {
            throw new NoSuchElementException();
         }

         Token token = consume();
         if (token == null) {
            throw new NoSuchElementException();
         }

         TokenType orig = token.type;
         if (token.type.isInstance(TokenType.URL)) {
            token = checkURL(token);
         } else if (abbreviations.isPrefixMatch(token.text)) {
            token = mergeAbbreviationAndAcronym(token);
         } else if (token.type.isInstance(TokenType.PUNCTUATION, TokenType.HYPHEN, TokenType.EMOTICON)) {
            token = handleEmoticon(token);
         } else if (token.type.isInstance(TokenType.MONEY) && peekIsType(0, TokenType.NUMBER)) {
            token = mergeMoneyNumber(token);
         } else if (token.type.isInstance(TokenType.NUMBER) && peekIsType(0, TokenType.MONEY)) {
            token = mergeMoneyNumber(token);
         }

         if (token.type.isInstance(TokenType.HYPHEN)) {
            token = mergeMultiHyphens(token);
         }

         token.index = lastIndex;
         lastIndex++;
         return token;
      }

      private Token peek(int distance) {
         while (buffer.size() <= distance) {
            try {
               Token token = tokenizer.next();

               if (token == null) {
                  return null;
               }

               if (!Strings.isNullOrBlank(token.text)) {
                  buffer.add(token);
               }
            } catch (IOException e) {
               throw new RuntimeException(e);
            }
         }
         return buffer.get(distance);
      }

      private boolean peekIsType(int distance, TokenType... types) {
         Token peeked = peek(distance);
         if (peeked == null) {
            return false;
         }
         return peeked.type.isInstance(types);
      }

   }

}//END OF EnglishTokenizer
