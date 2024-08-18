
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;
class Fenwick {
    private static int[] tree;

    public static void Fenwick(int n) {
        tree = new int[n];
    }

    public static void add(int i) {
        int n = tree.length;
        while (i < n) {
            tree[i]++;
            i += i & -i;
        }
    }

    public static int query(int i) {
        int res = 0;
        while (i > 0) {
            res += tree[i];
            i = i & (i - 1);
        }
        return res;
    }
}
//这里是注释
public class Main {
    static Scanner cin=new Scanner(System.in);
    public static void main(String[] args) {
        resultArray(new int[]{2,1,3,3});
    }
    public static int[] resultArray(int[] nums) {
        int[] sort = nums.clone();
        Arrays.sort(sort);
        int n = nums.length;
        Fenwick t1 = new Fenwick();
        t1.Fenwick(n + 1);
        Fenwick t2 = new Fenwick();
        t2.Fenwick(n + 1);

        List<Integer> a = new ArrayList<>(n);
        List<Integer> b = new ArrayList<>();
        t1.add(Arrays.binarySearch(sort, nums[0]) + 1);
        t2.add(Arrays.binarySearch(sort, nums[1]) + 1);
        a.add(nums[0]);
        b.add(nums[1]);
        for (int i = 2; i < n; i++) {
            int cnt1 = nums[i];
            int cnt=Arrays.binarySearch(sort,cnt1)+1;
            int k1 = a.size() - t1.query(cnt);
            int k2 = b.size() - t2.query(cnt);
            if (k1 > k2) {
                a.add(nums[i]);
                t1.add(Arrays.binarySearch(sort, nums[i]) + 1);
            } else if (k1 < k2) {
                b.add(nums[i]);
                t2.add(Arrays.binarySearch(sort, nums[i]) + 1);
            } else {
                if (a.size() <= b.size()) {
                    a.add(nums[i]);
                    t1.add(Arrays.binarySearch(sort, nums[i]) + 1);
                } else {
                    b.add(nums[i]);
                    t2.add(Arrays.binarySearch(sort, nums[i]) + 1);
                }
            }
        }
        a.addAll(b);
        for (int i = 0; i < n; i++) {
            nums[i] = a.get(i);
        }
        return nums;
    }
}
