package bcone;

import org.bouncycastle.util.encoders.Hex;

import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.KeyPair;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.sql.*;
import java.util.Date;
import java.util.*;

public class SignedBlockChain {

    private static int BC_LENGTH;
    private static List<BlockInfo> blockchain = new ArrayList<>();
    private static KeyPair keyPair;
    private static BlockInfoMapper blockInfoMapper = new BlockInfoMapper();

    public static void main(String[] args) {

        Scanner sc = new Scanner(System.in);

        BC_LENGTH = sc.nextInt();

        try {
            keyPair = Utils.loadKeys();
            makeBlockChain();

            try (Connection con = DriverManager
                    .getConnection("jdbc:postgresql://localhost:5432/postgres", "postgres", "1234")) {
                blockchain.forEach(block -> {
                    try {
                        save(block, con);
                    } catch (SQLException e) {
                        e.printStackTrace();
                    }
                });
                System.out.println("verification result: " + verification());

//                damage();


                List<BlockInfo> blockInfos = readAll(con, blockInfoMapper);
                print(blockInfos);

                clear(con);
            }

            System.out.println("verification result: " + verification());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void makeBlockChain() {
        byte[] prevHash = null;

        for (int i = 0; i < BC_LENGTH; i++) {
            BlockInfo blockInfo = new BlockInfo(i);
            blockInfo.getData().add("{\"data\":\"data " + i + "\"}");
            blockInfo.getData().add("{\"timestamp\":\"" + new Date() + "\"}");
            blockInfo.setPrevHash(prevHash);

            try {
                prevHash = Utils.getHash(blockInfo);

                blockInfo.setSign(Utils.generateRSAPSSSignature(keyPair.getPrivate(), prevHash));
                blockInfo.setSign2(Utils.generateRSAPSSSignature2(keyPair.getPrivate(), blockInfo.getData()));
            } catch (Exception e) {
                e.printStackTrace();
            }


            blockchain.add(blockInfo);
        }
    }

    private static void save(BlockInfo blockInfo, Connection con) throws SQLException {
        String createTable = "create table IF NOT EXISTS block_info(" +
                " blockNum int," +
                " data varchar," +
                " prev_hash bytea, " +
                " sign bytea, " +
                " sign2 bytea" +
                ")";

        String saveInfo = "insert into block_info (blockNum, data, prev_hash, sign, sign2) values (?, ?, ?, ?, ?)";

        try (Statement stmt = con.createStatement();
             PreparedStatement pstmt = con.prepareStatement(saveInfo)
        ) {
            stmt.execute(createTable);
            pstmt.setInt(1, blockInfo.getBlockNum());
            pstmt.setString(2, String.join(", ", blockInfo.getData()));
            pstmt.setBytes(3, blockInfo.getPrevHash());
            pstmt.setBytes(4, blockInfo.getSign());
            pstmt.setBytes(5, blockInfo.getSign2());
            pstmt.execute();
        }
    }

    private static List<BlockInfo> readAll(Connection connection, RowMapper<BlockInfo> mapper) {
        List<BlockInfo> result = new ArrayList<>();
        try (Statement stmt = connection.createStatement()) {
            String selectAll = "select * from block_info";
            stmt.setMaxRows(BC_LENGTH);
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
            String clearSql = "truncate block_info";
            stmt.execute(clearSql);
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
    }


    private static void print(List<BlockInfo> blocks) {
        blocks.forEach(bi -> {
            System.out.println("===================== " + bi.getBlockNum() + " =============================");
            System.out.println("prev hash: " + (bi.getPrevHash() != null ? new String(Hex.encode(bi.getPrevHash())) : ""));
            for (String s : bi.getData()) System.out.println(s);
            try {
                System.out.println("digest: " + new String(Hex.encode(Utils.getHash(bi))));
            } catch (NoSuchAlgorithmException | UnsupportedEncodingException | NoSuchProviderException e) {
                e.printStackTrace();
            }
            System.out.println("signature: " + new String(Hex.encode(bi.getSign())));
            System.out.println();
        });
    }

    private static boolean verification() throws GeneralSecurityException, UnsupportedEncodingException {

        byte[] prevHash = Utils.getHash(blockchain.get(0));
        for (int i = 1; i < BC_LENGTH; i++) {
            if (!Arrays.equals(prevHash, blockchain.get(i).getPrevHash())) {
                return false;
            }

            prevHash = Utils.getHash(blockchain.get(i));

            String data = "";
            for (String s : blockchain.get(i).getData()) {
                data = data + s;
            }

            if (!Utils.verifyRSAPSSSignature(keyPair.getPublic(), prevHash, blockchain.get(i).getSign())) {
                return false;
            }
            if (!Utils.verifyRSAPSSSignature(keyPair.getPublic(), data.getBytes(StandardCharsets.UTF_8), blockchain.get(i).getSign2())) {
                return false;
            }
        }

        return true;
    }

    private static void damage() {
        blockchain.get(3).getData().remove(0);
        blockchain.get(3).getData().add(0, "{\"data\":\"damaged data\"}");
    }

    private interface RowMapper<T> {
        T map(ResultSet rs) throws SQLException;
    }

    public static class BlockInfoMapper implements RowMapper<BlockInfo> {

        @Override
        public BlockInfo map(ResultSet rs) throws SQLException {

            BlockInfo info = new BlockInfo();
            info.setBlockNum(rs.getInt("blockNum"));
            info.setData(Arrays.asList(rs.getString("data").split(", ")));
            info.setPrevHash(rs.getBytes("prev_hash"));
            info.setSign(rs.getBytes("sign"));
            info.setSign2(rs.getBytes("sign2"));

            return info;
        }
    }
}
