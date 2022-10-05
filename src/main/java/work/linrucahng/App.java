package work.linrucahng;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.codec.Base64;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.lang.Console;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;

import java.io.File;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Hello world!
 */
public class App {

    /**
     * windows：
     *    java -jar nexus-upload.jar -l C:\Users\Administrator\Desktop\新建文件夹 -u admin -p admin123 -r http://192.168.19.107:8082/repository/lrc3
     *
     * linux：
     *   java -jar nexus-upload.jar  -l /www/project/nexus-upload/testRepo -u admin -p admin123 -r http://192.168.19.107:8082/repository/lrc3
     * @param args
     */
    public static void main(String[] args) {
        execScript(args);
        // execScript(new String[]{"-l", "C:\\Users\\Administrator\\Desktop\\新建文件夹", "-u", "admin", "-p", "admin123", "-r", "http://192.168.19.107:8082/repository/lrc3"});
    }

    public  static void execScript(String[] args) {
        NexusParam nexusParam = argsConvertToNexusParam(args);
        if (nexusParam.helpInfoFlag) {
            helpInfoLog();
            return;
        }else {
            Console.log("================入参=============================");
            Console.log("待上传的本地仓库地址：{}", nexusParam.getLocalRepositoryAbsolutePath());
            Console.log("Nexus账号：{}", nexusParam.getNexusUserAccount());
            Console.log("Nexus密码：{}", nexusParam.getNexusUserPassword());
            Console.log("远程仓库URL地址：{}", nexusParam.getNexusRepositoryUrl());
            Console.log("=============================================");
        }

        if (!checkEmptyNexusParam(nexusParam)) {
            return;
        }

        if (!checkNexusRepositoryUrlNetworkConnectivity(nexusParam)) {
            return;
        }

        List<File> uploadFiles = chekcLocalUploadFile(nexusParam);
        if (CollUtil.size(uploadFiles) == 0) {
            Console.log("\n本地仓库无可上传的文件，脚本结束");
            return;
        }


        sendOperator(nexusParam, uploadFiles);
    }


    /**
     * 入参转成NexusParam请求对象
     *
     * @param args
     * @return
     */
    public static NexusParam argsConvertToNexusParam(String[] args) {
        args = ObjectUtil.defaultIfNull(args, new String[0]);
        NexusParam nexusParam = new NexusParam();
        for (int i = 0; i < args.length; i = i + 2) {

            if (StrUtil.equalsAnyIgnoreCase(args[i], "help", "--help", "-h")) {
                nexusParam.setHelpInfoFlag(true);
            }

            if (i + 1 >= args.length) {
                break;
            }

            if (StrUtil.equalsAnyIgnoreCase(args[i], "-l", "-localRepositoryAbsolutePath", "localRepositoryAbsolutePath")) {
                nexusParam.setLocalRepositoryAbsolutePath(StrUtil.trim(args[i + 1]));
                if(StrUtil.isNotBlank(nexusParam.getLocalRepositoryAbsolutePath())) {
                    nexusParam.setLocalRepositoryAbsolutePath(StrUtil.addSuffixIfNot(nexusParam.getLocalRepositoryAbsolutePath(), "/"));
                }
            } else if (StrUtil.equalsAnyIgnoreCase(args[i], "-u", "nexusUserAccount", "-nexusUserAccount")) {
                nexusParam.setNexusUserAccount(StrUtil.trim(args[i + 1]));
            } else if (StrUtil.equalsAnyIgnoreCase(args[i], "-p", "nexusUserPassword", "-nexusUserPassword")) {
                nexusParam.setNexusUserPassword(StrUtil.trim(args[i + 1]));
            } else if (StrUtil.equalsAnyIgnoreCase(args[i], "-r", "nexusRepositoryUrl", "-nexusRepositoryUrl")) {
                nexusParam.setNexusRepositoryUrl(StrUtil.trim(args[i + 1]));
                if(StrUtil.isNotBlank(nexusParam.getNexusRepositoryUrl())) {
                    nexusParam.setNexusRepositoryUrl(StrUtil.addSuffixIfNot(nexusParam.getNexusRepositoryUrl(), "/"));
                }
            }
        }
        return nexusParam;
    }

    /**
     * 校验入参
     *
     * @param nexusParam 入参信息
     * @return 空则校验成功  不空校验失败信息
     */
    public static boolean checkEmptyNexusParam(NexusParam nexusParam) {
        boolean inValidFlag = BeanUtil.beanToMap(nexusParam).values()
                .stream()
                .anyMatch(ObjectUtil::hasEmpty);
        inValidFlag = inValidFlag || !FileUtil.exist(nexusParam.getLocalRepositoryAbsolutePath());

        if (inValidFlag) {
            Console.log("错误：可能Nexus的账号u、密码p、远程仓库地址r信息有缺失，或者本地仓库目录l不存在，请检查");
            helpInfoLog();
        } else {
            Console.log("入参非空校验通过！！！");
        }
        return !inValidFlag;
    }

    /**
     * 检测远程仓库的网络连通性
     *
     * @param nexusParam
     * @return
     */
    public static boolean checkNexusRepositoryUrlNetworkConnectivity(NexusParam nexusParam) {
        Console.log("\n================检测远程目录地址网络连通性, 请耐心等待=============================");
        boolean validFlag = true;
        try {
            int requestStatusCode = HttpRequest.put(nexusParam.getNexusRepositoryUrl())
                    .timeout(5000)
                    .execute()
                    .getStatus();
            validFlag = requestStatusCode == 401;
        }catch (Exception e) {
            validFlag = false;
        }

        if (validFlag) {
            Console.log("远程仓库【{}】访问通", nexusParam.getNexusRepositoryUrl());
        } else {
            Console.log("错误：远程仓库【{}】访问不通, 请检查", nexusParam.getNexusRepositoryUrl());
        }
        return validFlag;
    }

    public static List<File> chekcLocalUploadFile(NexusParam nexusParam) {
        Console.log("\n================待上传文件列表展示=============================");
        List<File> uploadFiles = FileUtil.loopFiles(nexusParam.getLocalRepositoryAbsolutePath(), currentFile -> {
                    return !StrUtil.containsAnyIgnoreCase(currentFile.getName(), "maven-metadata-local", "maven-metadata-deployment", "archetype-catalog") && !StrUtil.equalsIgnoreCase(FileUtil.extName(currentFile), "sh");
                }).stream()
                .peek(uploadFile -> {
                    Console.log(FileUtil.getAbsolutePath(uploadFile));
                }).collect(Collectors.toList());
        Console.log("文件个数：{}", CollUtil.size(uploadFiles));
        return uploadFiles;
    }


    /**
     * 帮助信息打印
     */
    public static void helpInfoLog() {
        Console.log("用法： java -jar nexus-upload.jar -l 本地仓库绝对路径 -u nexus账号 -p nexus密码 -r 远程仓库URL地址");
    }

    /**
     * 上传确认以及上传操作
     *
     * @param uploadFiles
     */
    public static void sendOperator(NexusParam nexusParam, List<File> uploadFiles) {
        Console.log("\n请检查上述文件路径是否是你需要上传的？【确定上传按y、取消上传按n】");

        // 等待用户输入
        while (true) {
            String userConfirm = StrUtil.trim(Console.input());
            if (StrUtil.equalsIgnoreCase(userConfirm, "n")) {
                Console.log("取消上传本地仓库文件，脚本结束");
                break;
            } else if (StrUtil.equalsIgnoreCase(userConfirm, "y")) {
                Console.log("\n================已上传文件列表展示=============================");
                uploadFiles.stream().forEach(uploadFile -> uploadSingleFile(nexusParam, uploadFile));
                Console.log("本地仓库文件上传结束，脚本结束");
                break;
            } else {
                Console.log("错误：【{}】非法字符，请根据提示输入对应的内容", userConfirm);
            }
        }

    }

    /**
     * 文件上传
     *
     * @param nexusParam
     * @param uploadFile
     */
    public static void uploadSingleFile(NexusParam nexusParam, File uploadFile) {
        try {
            String subPath = FileUtil.subPath(nexusParam.getLocalRepositoryAbsolutePath(), uploadFile);
            HttpResponse authorization = HttpRequest.put(nexusParam.getNexusRepositoryUrl() + subPath)
                    .header("Authorization", StrUtil.format("Basic {}", Base64.encode(StrUtil.format("{}:{}", nexusParam.getNexusUserAccount(), nexusParam.getNexusUserPassword()))), true)
                    .form("file", uploadFile)
                    .execute();
        } catch (Exception e) {

        }
        Console.log("已上传文件: {}", FileUtil.getAbsolutePath(uploadFile));
    }


}
