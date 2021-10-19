package bcone;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
public class BlockInfo {
    private int blockNum;
    private List<String> data = new ArrayList<>();
    private byte[] prevHash;
    private byte[] sign;
    private byte[] sign2;

    public BlockInfo(int blockNum) {
        this.blockNum = blockNum;
    }
}
