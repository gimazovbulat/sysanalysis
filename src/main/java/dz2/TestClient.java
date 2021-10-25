package dz2;

import bcone.Utils;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.util.encoders.Hex;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.security.*;
import java.security.spec.X509EncodedKeySpec;
import java.sql.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class TestClient {

    private static int bcLength = 10;

    private static List<BlockInfo> blockchain = new ArrayList<>();

    private static BlockInfoMapper blockInfoMapper = new BlockInfoMapper();

    /* 16-ричное представление публичного ключа сервиса */
    public static String publicKey = null;
    /* алгоритм ключа сервиса */
    public static final String KEY_ALGORITHM = "RSA";
    /* алгоритм подписи, формируемой сервисом */
    public static final String SIGN_ALGORITHM = "SHA256withRSAandMGF1";


    @SneakyThrows
    public static void main(String[] args) {

//        Scanner sc = new Scanner(System.in);
//
//        bcLength = sc.nextInt();

        makeBlockChain();
        readPublicKey();
        try (Connection con = DriverManager
                .getConnection("jdbc:postgresql://localhost:5432/postgres", "postgres", "1234")) {
            blockchain.forEach(block -> {
                try {
                    save(block, con);
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            });

            List<BlockInfo> blockInfos = readAll(con, blockInfoMapper);
            print(blockInfos);

        } catch (Exception e) {
            e.printStackTrace();
        }

        damage();
        boolean verification = verification(publicKey);
        System.out.println(verification);
    }

    /*
        Запрос публичного ключа с сервиса
     */
    public static void readPublicKey() {
        try {
            URL url = new URL("http://188.93.211.195/public");

            HttpURLConnection con = (HttpURLConnection) url.openConnection();

            con.setRequestMethod("GET");

            int rcode = con.getResponseCode();

            if (rcode == 200) {
                BufferedReader reader = new BufferedReader(new InputStreamReader(con.getInputStream()));

                publicKey = reader.readLine();

                System.out.println(publicKey);
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /*
        Запрос подписи хеша с меткой времени
     */
    public static TimeStampResp getSignatureAndTimeStamp(String digest) {
        TimeStampResp timeStampResp = null;
        try {
            URL url = new URL("http://188.93.211.195/ts?digest=" + digest);

            HttpURLConnection con = (HttpURLConnection) url.openConnection();

            con.setRequestMethod("GET");

            int rcode = con.getResponseCode();

            if (rcode == 200) {
                BufferedReader reader = new BufferedReader(new InputStreamReader(con.getInputStream()));

                String response = reader.readLine();

                ObjectMapper mapper = new ObjectMapper();
                timeStampResp = mapper.readValue(response, TimeStampResp.class);

            }

        } catch (IOException e) {
            e.printStackTrace();
        }
        return timeStampResp;
    }

    public static boolean verify(String publicKeyHexStr, byte[] data, String signHexStr) {
        Security.addProvider(new BouncyCastleProvider());

        try {
            Signature signature = Signature.getInstance(SIGN_ALGORITHM, "BC");

            X509EncodedKeySpec pubKeySpec = new X509EncodedKeySpec(Hex.decode(publicKeyHexStr));
            KeyFactory keyFactory = KeyFactory.getInstance(KEY_ALGORITHM);
            PublicKey pubKey = keyFactory.generatePublic(pubKeySpec);
            signature.initVerify(pubKey);

            signature.update(data);

            return signature.verify(Hex.decode(signHexStr));
        } catch (Exception e) {
            e.printStackTrace();
        }

        return false;
    }

    private static void makeBlockChain() {
        byte[] prevHash = null;
        try {
            for (int i = 0; i < bcLength; i++) {
                BlockInfo blockInfo = new BlockInfo(i);
                blockInfo.setData(String.valueOf(i));
                blockInfo.setPrevHash(prevHash);

                prevHash = Utils.getHash(blockInfo);
                String digest = new String(Hex.encode(prevHash), StandardCharsets.UTF_8);
                TimeStampResp signatureAndTimeStamp = getSignatureAndTimeStamp(digest);
                blockInfo.setSign(signatureAndTimeStamp.getTimeStampToken().getSignature());
                blockInfo.setTimeStamp(signatureAndTimeStamp.getTimeStampToken().getTs());

                blockchain.add(blockInfo);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private static void save(BlockInfo blockInfo, Connection con) throws SQLException {
        String createTable = "create table IF NOT EXISTS block_info2(" +
                " blockNum int," +
                " data varchar," +
                " prev_hash bytea, " +
                " sign varchar, " +
                " timestamp varchar " +
                ")";

        String saveInfo = "insert into block_info2 (blockNum, data, prev_hash, sign, timestamp) values (?, ?, ?, ?, ?)";

        try (Statement stmt = con.createStatement();
             PreparedStatement pstmt = con.prepareStatement(saveInfo)
        ) {
            stmt.execute(createTable);
            pstmt.setInt(1, blockInfo.getBlockNum());
            pstmt.setString(2, String.join(", ", blockInfo.getData()));
            pstmt.setBytes(3, blockInfo.getPrevHash());
            pstmt.setString(4, blockInfo.getSign());
            pstmt.setString(5, blockInfo.getTimeStamp());
            pstmt.execute();
        }
    }

    private static List<BlockInfo> readAll(Connection connection, RowMapper<BlockInfo> mapper) {
        List<BlockInfo> result = new ArrayList<>();
        try (Statement stmt = connection.createStatement()) {
            String selectAll = "select * from block_info2";
            stmt.setMaxRows(bcLength);
            ResultSet resultSet = stmt.executeQuery(selectAll);
            while (resultSet.next()) {
                BlockInfo info = mapper.map(resultSet);
                result.add(info);
            }
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }

        return result;
    }

    private static void clear(Connection connection) {
        try (Statement stmt = connection.createStatement()) {
            String clearSql = "truncate block_info2";
            stmt.execute(clearSql);
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
    }

    private static void print(List<BlockInfo> blocks) {
        blocks.forEach(bi -> {
            System.out.println("===================== " + bi.getBlockNum() + " =============================");
            System.out.println("prev hash: " + (bi.getPrevHash() != null ? new String(Hex.encode(bi.getPrevHash())) : ""));
            System.out.println(bi.getData());
            try {
                System.out.println("digest: " + new String(Hex.encode(Utils.getHash(bi))));
            } catch (NoSuchAlgorithmException | UnsupportedEncodingException | NoSuchProviderException e) {
                e.printStackTrace();
            }
            System.out.println("signature: " + new String(Hex.encode(bi.getSign().getBytes(StandardCharsets.UTF_8))));
            System.out.println();
        });
    }

    private static boolean verification(String publicKey) throws GeneralSecurityException, UnsupportedEncodingException {
        byte[] prevHash = Utils.getHash(blockchain.get(0));
        for (int i = 1; i < bcLength; i++) {
            BlockInfo blockInfo = blockchain.get(i);

            String digest = new String(Hex.encode(Utils.getHash(blockInfo)), StandardCharsets.UTF_8);
            byte[] data = new byte[blockInfo.getTimeStamp().getBytes().length + Hex.decode(digest).length];

            System.arraycopy(blockInfo.getTimeStamp().getBytes(), 0, data, 0, blockInfo.getTimeStamp().getBytes().length);
            System.arraycopy(Hex.decode(digest), 0, data, blockInfo.getTimeStamp().getBytes().length, Hex.decode(digest).length);

            if (!Arrays.equals(prevHash, blockchain.get(i).getPrevHash())) {
                return false;
            }
            prevHash = Utils.getHash(blockchain.get(i));

            boolean isOk = verify(
                    publicKey,
                    data,
                    blockInfo.getSign()
            );

            if (!isOk) return false;
        }

        return true;
    }

    private static void damage() {
        blockchain.get(3).setData("damaged");
    }

    private interface RowMapper<T> {
        T map(ResultSet rs) throws SQLException;
    }

    public static class BlockInfoMapper implements RowMapper<BlockInfo> {

        @Override
        public BlockInfo map(ResultSet rs) throws SQLException {

            BlockInfo info = new BlockInfo();
            info.setBlockNum(rs.getInt("blockNum"));
            info.setData(rs.getString("data"));
            info.setPrevHash(rs.getBytes("prev_hash"));
            info.setSign(rs.getString("sign"));
            info.setTimeStamp(rs.getString("timestamp"));

            return info;
        }
    }
}
