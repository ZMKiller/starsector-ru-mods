package data.scripts.campaign.intel.bar;

import com.fs.starfarer.api.util.WeightedRandomPicker;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.lazywizard.lazylib.MathUtils;

public abstract class CabalDiceGame {

    public static final Map<String, CabalDiceGame> GAMES = new HashMap<>();
    public static final List<String> GAMES_LIST = new ArrayList<>();

    static {
        addGame("allPrime", new AllPrime());
        addGame("sequential", new Sequential());
        addGame("powerOf2", new PowerOfTwo());
        addGame("min144", new Min144());
        //addGame("min180", new Min180());
    }

    public static void addGame(String id, CabalDiceGame game) {
        GAMES.put(id, game);
        GAMES_LIST.add(id);
    }

    public static CabalDiceGame getGame(String id) {
        return GAMES.get(id);
    }

    public static String getRandomGameId() {
        WeightedRandomPicker<String> picker = new WeightedRandomPicker<>();
        picker.addAll(GAMES_LIST);
        return picker.pick();
    }

    public static int[] roll() {
        int[] dice = new int[3];
        for (int i = 0; i < 3; i++) {
            dice[i] = MathUtils.getRandomNumberInRange(1, 6);
        }
        return dice;
    }

    /**
     * Gets a (usually pre-defined) roll that meets the winning conditions.
     *
     * @return
     */
    public int[] getDebugRoll() {
        return new int[]{1, 1, 1};
    }

    /**
     * Does the provided dice roll result meet the win condition?
     *
     * @param dice
     * @return
     */
    public abstract boolean isWinner(int[] dice);

    public abstract String getRuleString();

    public abstract int getWinningsMult();

    public boolean printSum() {
        return false;
    }

    public boolean printProduct() {
        return false;
    }

    // odds: 1/8 or 27/216
    public static class AllPrime extends CabalDiceGame {

        @Override
        public boolean isWinner(int[] dice) {
            for (int i = 0; i < 3; i++) {
                if (!isPrime(dice[i])) {
                    return false;
                }
            }
            return true;
        }

        public boolean isPrime(int num) {
            return num == 2 || num == 3 || num == 5;
        }

        @Override
        public String getRuleString() {
            return "All numbers are prime";
        }

        @Override
        public int getWinningsMult() {
            return 4;
        }

        @Override
        public int[] getDebugRoll() {
            return new int[]{2, 3, 5};
        }
    }

    // odds: 1/9 or 24/216
    public static class Sequential extends CabalDiceGame {

        @Override
        public boolean isWinner(int[] dice) {
            int[] copy = dice.clone();
            Arrays.sort(copy);
            return (copy[1] == copy[0] + 1) && (copy[2] == copy[1] + 1);
        }

        @Override
        public String getRuleString() {
            return "Three sequential numbers";
        }

        @Override
        public int getWinningsMult() {
            return 5;
        }

        @Override
        public int[] getDebugRoll() {
            return new int[]{6, 4, 5};
        }
    }

    // odds: 5/36 or 30/216
    public static class PowerOfTwo extends CabalDiceGame {

        @Override
        public boolean isWinner(int[] dice) {
            int sum = dice[0] + dice[1] + dice[2];
            // hardcore bitwise operator taken from StackOverflow... 
            // we could have just checked if it equals 4, 8 or 16
            return (sum & (sum - 1)) == 0;
        }

        @Override
        public String getRuleString() {
            return "Sum of dice is a power of two";
        }

        @Override
        public int getWinningsMult() {
            return 4;
        }

        @Override
        public int[] getDebugRoll() {
            return new int[]{2, 3, 3};
        }

        @Override
        public boolean printSum() {
            return true;
        }
    }

    // odds: 5/108 or 10/216
    public static class Min144 extends CabalDiceGame {

        @Override
        public boolean isWinner(int[] dice) {
            return dice[0] * dice[1] * dice[2] >= 144;
        }

        @Override
        public String getRuleString() {
            return "Product of dice is at least 144";
        }

        @Override
        public int getWinningsMult() {
            return 11;
        }

        @Override
        public int[] getDebugRoll() {
            return new int[]{6, 5, 5};
        }

        @Override
        public boolean printProduct() {
            return true;
        }
    }

    // odds: 1/54 or 4/216
    public static class Min180 extends CabalDiceGame {

        @Override
        public boolean isWinner(int[] dice) {
            return dice[0] * dice[1] * dice[2] >= 180;
        }

        @Override
        public String getRuleString() {
            return "Product of dice is at least 180";
        }

        @Override
        public int getWinningsMult() {
            return 27;
        }

        @Override
        public int[] getDebugRoll() {
            return new int[]{6, 6, 5};
        }

        @Override
        public boolean printProduct() {
            return true;
        }
    }
}
