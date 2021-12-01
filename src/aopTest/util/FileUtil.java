package aopTest.util;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@SuppressWarnings({"SameParameterValue","unused","UnusedReturnValue"})
public class FileUtil {
    private String projectPath;
    private String basePath;

    public FileUtil(){
        init();
        this.basePath = projectPath;
    }

    public FileUtil(String basePackage){
        init();
        this.basePath = this.projectPath + "\\"+ list2String(new ArrayList<>(Arrays.asList(basePackage.split("\\."))),"\\");
    }

    public void setBasePath(String basePackage){
        this.basePath = this.projectPath + "\\"
                + list2String(new ArrayList<>(Arrays.asList(basePackage.split("\\."))),"\\");
    }

    private void init(){
        this.projectPath = getProjectPath()+"\\out\\production";
        Path path = Paths.get(Objects.requireNonNull(this.getProjectPath()));
        this.projectPath += "\\"+path.getFileName();
    }

    /**
     * 获得当前项目的文件路径
     * @return 项目的文件路径
     */
    private String getProjectPath() {
        try {
            File pwd = new File("");
            return pwd.getCanonicalPath();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 递归查找basePath下的所有后缀为suffix的文件。
     * @param basePath 基路径
     * @param suffix 后缀
     * @return 文件列表
     */
    private List<Path> getFileBySuffix(String basePath,String suffix){
        try{
            Pattern pattern = Pattern.compile("^.+\\."+suffix);
            Path path = Paths.get(basePath);
            return Files.walk(path).filter(p ->{
                if(!Files.isRegularFile(p))
                    return false;
                File file = p.toFile();
                Matcher matcher = pattern.matcher(file.getName());
                return matcher.matches();
            }).collect(Collectors.toList());
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 将String的列表拼接成起来，以指定分隔符间隔
     * @param list String列表
     * @param separator 分隔符
     * @return item+separator
     */
    private String list2String(List<String> list, String separator){
        StringBuilder sb = new StringBuilder();
        list.forEach(item -> {sb.append(item); sb.append(separator);});
        //消除最后一个点
        return sb.toString().substring(0,sb.length() - separator.length());
    }

    /**
     * 将文件路径转化成类名
     * @param filePath 文件路径
     * @return 类名
     */
    private String convertFilePath2ClassName(String filePath){
        List<String> classPackageList = new ArrayList<>(Arrays.asList(filePath.split("\\\\")));
        List<String> projectPath = new ArrayList<>(Arrays.asList(this.projectPath.split("\\\\")));
        projectPath.forEach(classPackageList::remove);
        //去除后缀
        String last = classPackageList.remove(classPackageList.size()-1);
        classPackageList.add(last.split("\\.")[0]);
        return list2String(classPackageList,".");
    }

    public List<String> getClassNames(){
        String basePath = this.basePath;
        List<Path> paths = getFileBySuffix(basePath,"class");
        assert paths != null;
        List<String> classNames = new ArrayList<>();
        for(Path path : paths){
            classNames.add(convertFilePath2ClassName(path.toString()));
        }
        return classNames;
    }
}
