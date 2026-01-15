/**
 * Wire
 * Copyright (C) 2018 Wire Swiss GmbH
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package anubhav.calculatorapp;

/**
 * Created by Anubhav on 14-03-2016.
 */
public class CalculateFactorial
{
    public static final int MAX=1000;

    private int res_size;
    private int res[]=new int[MAX];

    CalculateFactorial()
    {
        res_size = 1;
    }

    public int getRes()
    {
        return res_size;
    }

    // This function finds factorial of large numbers and prints them
    public int[] factorial(int n)
    {
        // Initialize result
        res[0] = 1;

        // Apply simple factorial formula n! = 1 * 2 * 3 * 4...*n
        for (int x=2; x<=n; x++)
            res_size = multiply(x, res_size);

        return res;
    }

    // This function multiplies x with the number represented by res[].
// res_size is size of res[] or number of digits in the number represented
// by res[]. This function uses simple school mathematics for multiplication.
// This function may value of res_size and returns the new value of res_size
    private int multiply(int x, int r)
    {
        int carry = 0;  // Initialize carry

        // One by one multiply n with individual digits of res[]
        for (int i=0; i<r; i++)
        {
            int prod = res[i] * x + carry;
            res[i] = prod % 10;  // Store last digit of 'prod' in res[]
            carry  = prod/10;    // Put rest in carry
        }

        // Put carry in res and increase result size
        while (carry!=0)
        {
            res[r] = carry%10;
            carry = carry/10;
            r++;
        }
        return r;
    }
}
