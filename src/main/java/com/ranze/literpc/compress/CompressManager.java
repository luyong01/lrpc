package com.ranze.literpc.compress;

import static com.ranze.literpc.compress.Compress.*;

public class CompressManager {

    private static volatile CompressManager INSTANCE;

    private Compress noneCompress;
    private Compress gzipCompress;
    private Compress zlibCompress;

    private CompressManager() {
        noneCompress = new NoneCompress();
        gzipCompress = new GzipCompress();
        zlibCompress = new ZlibCompress();

    }

    public static CompressManager getInstance() {
        if (INSTANCE == null) {
            synchronized (CompressManager.class) {
                if (INSTANCE == null) {
                    INSTANCE = new CompressManager();
                }
            }
        }
        return INSTANCE;
    }

    public Compress.Type convert(int compressTypeNo) {
        if (compressTypeNo == Type.NONE.getTypeNo()) {
            return Type.NONE;
        } else if (compressTypeNo == Type.GZIP.getTypeNo()) {
            return Type.GZIP;
        } else if (compressTypeNo == Type.ZLIB.getTypeNo()) {
            return Type.ZLIB;
        }

        throw new RuntimeException("Unsupported compress type num: " + compressTypeNo);

    }

    public Compress get(int compressTypeNo) {
        if (compressTypeNo == Type.NONE.getTypeNo()) {
            return noneCompress;
        } else if (compressTypeNo == Type.GZIP.getTypeNo()) {
            return gzipCompress;
        } else if (compressTypeNo == Type.ZLIB.getTypeNo()) {
            return zlibCompress;
        }

        throw new RuntimeException("Unsupported compress type num: " + compressTypeNo);
    }

    public Compress get(Compress.Type compressType) {
        if (compressType == Type.NONE) {
            return noneCompress;
        } else if (compressType == Type.GZIP) {
            return gzipCompress;
        } else if (compressType == Type.ZLIB) {
            return zlibCompress;
        }
        throw new RuntimeException("Unsupported compress type: " + compressType.getTypeName());
    }
}
