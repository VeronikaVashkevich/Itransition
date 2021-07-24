package game;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.io.UnsupportedEncodingException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Scanner;

public class Game {
    private static final String HMAC_ALGO = "HmacSHA3-256";

    private static byte[] generateBytes(){
        SecureRandom secureRandom = new SecureRandom();
        byte []bytes = new byte[16];
        secureRandom.nextBytes(bytes);
        return bytes;
    }

    private static Mac generateKey(byte[] bytes) throws NoSuchAlgorithmException, InvalidKeyException {
        Mac signer = Mac.getInstance(HMAC_ALGO);
        SecretKeySpec keySpec = new SecretKeySpec(bytes, HMAC_ALGO);
        signer.init(keySpec);

        return signer;
    }

    public static String bytesToHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder(bytes.length*2);
        for(byte b: bytes) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }

    public static int calcMoves(int playerMove, int compMove, int mid){
        if(compMove == playerMove){
            return 0;
        }
        else if(playerMove <= mid){
            for(int i = playerMove; i <= playerMove + mid; i++){
                if(i == compMove){
                    return -1;
                }
                else{
                    return 1;
                }
            }
        }
        else if(playerMove > mid){
            for(int i = playerMove - mid; i <= playerMove; i++){
                if(i == compMove){
                    return 1;
                }
                else{
                    return -1;
                }
            }
        }
        return 0;
    }

    private static void showMenu(String [] arguments){
        System.out.println("Available moves: ");
        for(int i = 0; i < arguments.length; i++){
            System.out.println((i + 1) + " - " + arguments[i]);
        }
        System.out.println("0 - exit");
    }

    public static void main(String[] args) throws NoSuchAlgorithmException, InvalidKeyException, UnsupportedEncodingException {

        if(args.length < 3 || args.length % 2 == 0){
            System.out.println("Invalid input (the number of input parameters must be odd or > 2)");
            System.exit(0);
        }
        for(int i = 0; i < args.length; i++){
            for(int j = i+1; j < args.length; j++){
                if(args[i].equals(args[j])){
                    System.out.println("Invalid input (input parameters are repeated)");
                    System.exit(0);
                }
            }
        }

        //generate key
        byte[] bytes = generateBytes();
        Mac signer = generateKey(bytes);

        //computer makes his move
        int computerMoveNum = (int)(Math.random() * (double) args.length + 1);
        String computerMove = args[computerMoveNum - 1];

        //hmac
        byte[] hmac = signer.doFinal(computerMove.getBytes("utf-8"));
        System.out.println("HMAC: " + bytesToHex(hmac));

        //player menu
        Scanner scanner = new Scanner(System.in);
        showMenu(args);
        int playerMove = scanner.nextInt();

        while(playerMove < 0 || playerMove > (args.length)){
            showMenu(args);
            playerMove = scanner.nextInt();
        }

        //results
        if(playerMove == 0){
            System.exit(0);
        }
        else{
            System.out.println("Your move: " + args[playerMove - 1]);
            System.out.println("Computer move: " + args[computerMoveNum - 1]);

            int middle = args.length / 2;
            int result = calcMoves(playerMove, computerMoveNum, middle);
            switch (result){
                case -1:
                    System.out.println("Computer win!");
                    break;
                case 1:
                    System.out.println("You win!");
                    break;
                case 0:
                    System.out.println("Draw!");
                    break;
            }
            System.out.println("HMAC key: " + bytesToHex(bytes));

        }
    }

}
