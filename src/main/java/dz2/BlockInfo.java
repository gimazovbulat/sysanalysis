package dz2;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
public class BlockInfo {

    private int blockNum;

    private String data;

    private byte[] prevHash;

    private String sign;

    private String timeStamp;

    public BlockInfo(int blockNum) {
        this.blockNum = blockNum;
    }
}
