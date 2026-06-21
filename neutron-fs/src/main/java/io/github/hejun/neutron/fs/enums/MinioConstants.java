package io.github.hejun.neutron.fs.enums;

/**
 * MinIO 枚举常量
 *
 * @author HeJun
 */
public interface MinioConstants {

    interface ExpireTimes {
        int DAY_1 = 1;
        int DAY_3 = 3;
        int DAY_7 = 7;
        int DAY_15 = 15;
        int MONTH_1 = 30;
        int MONTH_3 = 90;
        int MONTH_6 = 180;
        int YEAR_1 = 365;
    }

    interface FILE_ATTR {
        String ORIGINAL_NAME = "original-filename";
        String EXPIRE_DAYS = "expire-days";
    }

}
