package com.lyyzoo.fastdfs.client;

import org.apache.commons.pool2.BasePooledObjectFactory;
import org.apache.commons.pool2.PooledObject;
import org.apache.commons.pool2.impl.DefaultPooledObject;
import org.csource.fastdfs.StorageClient1;
import org.csource.fastdfs.StorageServer;
import org.csource.fastdfs.TrackerClient;
import org.csource.fastdfs.TrackerServer;

import java.io.IOException;

/**
 * StorageClient1 工厂类，创建对象池需要 BasePooledObjectFactory 对象或子类
 * <pre>
 * 上传文件使用 StorageClient1, StorageClient1 需要创建 StorageServer 来连接服务器，
 * StorageServer 使用比较频繁且创建连接比较耗时，所以将其池化。
 * StorageServer 或 TrackerServer 为null，会自动获取，使用完后，会自动关闭连接；如果不为null则不会关闭。
 * </pre>
 *
 * @author jiangzhou.bo@hand-china.com
 * @version 1.0
 * @name FastDFSPooledObjectFactory
 * @date 2017-10-14 14:45
 */
public class StorageClient1Factory extends BasePooledObjectFactory<StorageClient1> {

    @Override
    public StorageClient1 create() throws Exception {
        // TrackerClient
        TrackerClient trackerClient = new TrackerClient();
        // TrackerServer
        TrackerServer trackerServer = null;
        // return
        StorageClient1 storageClient1 = null;
        try {
            trackerServer = trackerClient.getConnection();
            if(trackerServer == null){
                throw new FastDFSException(ErrorCode.FILE_SERVER_CONNECTION_FAILED.CODE, ErrorCode.FILE_SERVER_CONNECTION_FAILED.MESSAGE);
            }
            // 创建 StorageServer 这样才不会关闭连接
            StorageServer storageServer = trackerClient.getStoreStorage(trackerServer);
            // StorageClient1 获取StorageServer连接的客户端
            storageClient1 = new StorageClient1(trackerServer, storageServer);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if(trackerServer != null){
                trackerServer.close();
            }
        }

        return storageClient1;
    }

    @Override
    public PooledObject<StorageClient1> wrap(StorageClient1 client) {
        return new DefaultPooledObject<StorageClient1>(client);
    }
}
