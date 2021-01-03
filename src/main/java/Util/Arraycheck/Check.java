package Util.Arraycheck;

import java.util.List;

public abstract class Check {
    /* Method return minimum value*/
    private static int getMinimum(int[] arr, int n)
    {
        int min = arr[0];
        for (int i = 1; i < n; i++)
            if (arr[i] < min)
                min = arr[i];
        return min;
    }

    /* Method return maximum value*/
    private static int getMaximum(int[] arr, int n)
    {
        int max = arr[0];
        for (int i = 1; i < n; i++)
            if (arr[i] > max)
                max = arr[i];
        return max;
    }

    /* This method checks if array elements are consecutive */
    public static boolean checkArrayContainsConsecutiveElements(int[] arr, int n)
    {
        if ( n <  1 )
            return false;

        int min = getMinimum(arr, n);
        int max = getMaximum(arr, n);

        if (max - min  + 1 == n)
        {
            boolean[] visited=new boolean[arr.length];
            for (int i = 0; i < n; i++)
            {
                if (visited[arr[i] - min])
                    return false;

                visited[arr[i] - min] = true;
            }

            return true;
        }
        return false;
    }
    public static boolean checkListContainsConsecutiveElements(List<Integer> List)
    {
        int[] arr = List.stream().mapToInt( Integer::intValue ).toArray();
        return checkArrayContainsConsecutiveElements( arr, arr.length );
    }
}
