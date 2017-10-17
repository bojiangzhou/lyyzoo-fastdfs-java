package com.lyyzoo.fastdfs.client;

import org.apache.commons.pool2.impl.GenericObjectPool;
import org.apache.commons.pool2.impl.GenericObjectPoolConfig;
import org.csource.common.MyException;
import org.csource.fastdfs.ClientGlobal;
import org.csource.fastdfs.StorageClient1;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;

import java.io.IOException;

/**
 * StorageClient1 对象池
 * <p>
 *
 * @author jiangzhou.bo@hand-china.com
 * @version 1.0
 * @name StorageClient1Pool
 * @date 2017-10-14 15:23
 */
public class StorageClient1Pool {
    /**
     * org.slf4j.Logger
     */
    private static Logger logger = LoggerFactory.getLogger(StorageClient1Pool.class);

    /**
     * TrackerServer 配置文件路径
     */
    private static final String FASTDFS_CONFIG_PATH = "config.properties";

    /**
     * 最大连接数 default 8.
     */
    @Value("${max_storage_connection}")
    private static int maxStorageConnection;

    /**
     * StorageClient1 对象池.
     * GenericObjectPool 没有无参构造
     */
    private static GenericObjectPool<StorageClient1> storageClientPool;

    private StorageClient1Pool(){};

    private static synchronized GenericObjectPool<StorageClient1> getObjectPool(){
        if(storageClientPool == null){
            try {
                // 加载配置文件
                ClientGlobal.initByProperties(FASTDFS_CONFIG_PATH);
            } catch (IOException e) {
                e.printStackTrace();
            } catch (MyException e) {
                e.printStackTrace();
            }

            if(logger.isDebugEnabled()){
                logger.debug("ClientGlobal configInfo: {}", ClientGlobal.configInfo());
            }

            // Pool配置
            GenericObjectPoolConfig poolConfig = new GenericObjectPoolConfig();
            poolConfig.setMinIdle(2);
            if(maxStorageConnection > 0){
                poolConfig.setMaxTotal(maxStorageConnection);
            }

            storageClientPool = new GenericObjectPool<>(new StorageClient1Factory(), poolConfig);
        }
        return storageClientPool;
    }

    /**
     * 获取 StorageClient1
     * @return StorageClient1
     * @throws FastDFSException
     */
    public static StorageClient1 borrowObject() throws FastDFSException {
        StorageClient1 storageClient1 = null;
        try {
            storageClient1 = getObjectPool().borrowObject();
        } catch (Exception e) {
            e.printStackTrace();
            if(e instanceof FastDFSException){
                throw (FastDFSException) e;
            }
        }
        return storageClient1;
    }

    /**
     * 回收 StorageClient1
     * @param storageClient1 需要回收的 StorageClient1
     */
    public static void returnObject(StorageClient1 storageClient1){
        getObjectPool().returnObject(storageClient1);
    }


}
