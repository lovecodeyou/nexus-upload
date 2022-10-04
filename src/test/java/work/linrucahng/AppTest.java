package work.linrucahng;


import cn.hutool.core.lang.Console;
import cn.hutool.core.util.StrUtil;
import org.junit.Test;

/**
 * Unit test for simple App.
 */
public class AppTest {

    /**
     * 帮助信息
     */
    @Test
    public void execScriptTest() {
        App.main(new String[]{"help"});
    }

    /**
     * 非空校验
     */
    @Test
    public void execScriptUploadError1Test() {
        App.main(new String[]{"-l", "", "-u", "admin", "-p", "admin12322", "-r", "http://192.168.19.107:8082/repository/lrc3/"});
    }

    /**
     * 远程仓库网络连通性
     */
    @Test
    public void execScriptUploadError2Test() {
        App.main(new String[]{"-l", "C:\\Users\\Administrator\\Desktop\\新建文件夹", "-u", "admin", "-p", "admin123", "-r", "http://192.168.19.107:8082/repository/fdsfds"});
    }

    /**
     * 本地仓库目录是否存在
     */
    @Test
    public void execScriptUploadError3Test() {
        App.main(new String[]{"-l", "C:\\Users\\Administrator\\Desktop\\新建文件夹2", "-u", "admin", "-p", "admin123", "-r", "http://192.168.19.107:8082/repository/lrc3"});
    }

    /**
     * 本地仓库目录存在但无文件
     */
    @Test
    public void execScriptUploadSuccess1Test() {
        App.main(new String[]{"-l", "C:\\Users\\Administrator\\Desktop\\新建文件夹 (2)", "-u", "admin", "-p", "admin123", "-r", "http://192.168.19.107:8082/repository/lrc3"});
    }


    /**
     * 本地仓库目录存在但无文件
     */
    @Test
    public void execScriptUploadSuccess2Test() {
        App.main(new String[]{"-l", "C:\\Users\\Administrator\\Desktop\\新建文件夹", "-u", "admin", "-p", "admin123", "-r", "http://192.168.19.107:8082/repository/lrc3"});
    }

    @Test
    public void test1() {

        String[] commands = {"-l", "C:\\Users\\Administrator\\Desktop\\新建文件夹", "-u", "admin", "-p", "admin123", "-r", "http://192.168.19.107:8082/repository/lrc3"};

        Console.log(StrUtil.join(StrUtil.SPACE,commands));
    }
}
