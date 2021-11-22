/*
 * Copyright (c) 2021-2021 Svegon and contributors.
 *
 * This source code is subject to the terms of the GNU General Public
 * License, version 3. If a copy of the GPL was not distributed with this
 * file, You can obtain one at: https://www.gnu.org/licenses/gpl-3.0.txt
 */
package svegon.utils.math;

import it.unimi.dsi.fastutil.chars.CharPredicate;

import java.util.List;
import java.util.Random;

public class ExtendedRandom extends Random {
    public char nextChar() {
        return (char) next(Character.SIZE);
    }

    public String nextString(final int length) {
        char[] charArray = new char[length];

        for (int i = 0; i < length; ++i) {
            charArray[i] = nextChar();
        }

        return new String(charArray);
    }

    public String nextString(final int length, final CharPredicate validator) {
        char[] charArray = new char[length];

        for (int i = 0; i < length; ++i) {
            char chr = nextChar();

            while (!validator.test(chr)) {
                chr = nextChar();
            }

            charArray[i] = chr;
        }

        return new String(charArray);
    }

    public boolean nextBoolean(int in) {
        return nextInt(in) == 0;
    }

    public boolean nextBoolean(double chance) {
        return nextGaussian() < chance;
    }

    public <E> E randElement(List<E> range) {
        return range.get(nextInt(range.size()));
    }

    public <E> E randElement(E[] array) {
        return array[nextInt(array.length)];
    }
}
