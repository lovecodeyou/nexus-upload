package work.linrucahng;

import lombok.Data;

/**
 * @author LinRuChang
 * @version 1.0
 * @date 2022/10/05
 * @since 1.8
 **/
@Data
public class NexusParam {

    /**
     * nexus账号
     */
    String nexusUserAccount;

    /**
     * nexus账号
     */
    String nexusUserPassword;

    /**
     * 本地仓库绝对路径
     */
    String localRepositoryAbsolutePath;

    /**
     * 远程仓库URL地址
     */
    String nexusRepositoryUrl;

    /**
     * 是否是help信息命令
     */
    boolean helpInfoFlag;
}
