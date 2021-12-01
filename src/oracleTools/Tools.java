package oracleTools;

import java.io.*;
import java.sql.*;
import java.util.*;

public class Tools {
    public final static String NOW = "sicp3";
    public final static String YL = "sbwssb";
    public final static String SY = "wssb_cs";

    public final static String[] tables = {"WEBLOG","AA03","AA10","AA17",
            "AA71","AB01","AB01_LHJY","AB01A1","AB02","ABF1","AC01","AC01A1"
            ,"AC02","AC08","AC20","AC30","AC31","AC50","AC51","AC52","AC53",
            "AC60","AC61","AC71","AC73","AC74","AC77","AC82","AC83","AD21",
            "AE02","AF01","AF01A1","IC10","SYSEXTSUBCENTER"};
    public final static List<String> tableList = Arrays.asList(tables);
    public final static String[] nowTables = {"AA03","AA10","AA17",
            "AA71","AB01","AB01A1","AB02","ABF1","AC01","AC01A1"
            ,"AC02","AC08","AC20","AC30","AC31","AC50","AC51","AC52","AC53",
            "AC60","AC61","AC71","AC73","AC74","AC77","AC82","AC83","AD21",
            "AE02","AF01","AF01A1","IC10"
    };
    public final static List<String> nowTableList = Arrays.asList(nowTables);
    private Connection getConnection(String userName){
        String driverClass = "oracle.jdbc.driver.OracleDriver";
        Connection connection = null;
        String url = null;
        String username = null;
        String passwd = null;
        try{
            Class.forName(driverClass);
            if(userName.equals("sbwssb")){
                url = "jdbc:oracle:thin:@10.96.10.60:1521:sbywqz";
                username = "sbwssb";
                passwd = "sbwssb#19SI";
                connection = DriverManager.getConnection(url,username,passwd);
            }
            else {
                if(userName.equals("sicp3")){
                    url = "jdbc:oracle:thin:@10.16.12.41:1521:orcl";
                    username = "sicp3";
                    passwd = "sicp3test";
                }
                else{
                    url = "jdbc:oracle:thin:@10.96.1.75:1521:testfjjy";
                    username = "wssb_cs";
                    passwd = "1qaz_wsx";
                }
            }
            connection = DriverManager.getConnection(url,username,passwd);
        } catch (ClassNotFoundException | SQLException e) {
            e.printStackTrace();
        }
        return connection;
    }

    private List<String> getTableNames(String username){
        Connection conn = getConnection(username);
        Statement st = null;
        ResultSet res = null;
        List<String> tables = new ArrayList<>();
        try{
            st = conn.createStatement();
            String sql = "select * from user_tables";
            res = st.executeQuery(sql);
            while (res.next()){
                String tableName = res.getString("table_Name");
                String tableSpaceName = res.getString("tableSpace_name");
//                tables.add(tableSpaceName + "__" + tableName);
                tables.add(tableName);
            }
            res.close();
            st.close();
            conn.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return tables;
   }


   private List<String> getTableStruct(String username , String tableName){
        String sql = "SELECT T1.COLUMN_NAME, T2.COMMENTS " +
                "FROM USER_TAB_COLS T1, USER_COL_COMMENTS T2 " +
                "WHERE T1.TABLE_NAME = T2.TABLE_NAME AND T1.COLUMN_NAME = T2.COLUMN_NAME AND" +
                " T1.TABLE_NAME =upper('"+tableName+"')";
        Connection conn = getConnection(username);
        List<String> list = new ArrayList<>();
        try{
            Statement st = conn.createStatement();
            ResultSet res = st.executeQuery(sql);
            while (res.next()){
                String column = res.getString("COLUMN_NAME");
                String comments = res.getString("COMMENTS");
                list.add(column + "    " + comments);
            }
            res.close();
            st.close();
            conn.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
   }

   private void main(){
        //获得三个库的所有表的名字
        List<String> sicp3TableNames = getTableNames(NOW);
        List<String> ylTableNames = getTableNames(YL);
        List<String> syTableNames = getTableNames(SY);
        //对比两个列表   获得相同的表名
       List<String> si_ylList = new ArrayList<>();
       List<String> si_syList = new ArrayList<>();
       List<String> si_yl_except = new ArrayList<>();
       List<String> si_sy_except = new ArrayList<>();
       Boolean flag = false;
       for(String yl : ylTableNames){
           for(String si : sicp3TableNames){
               if(yl.equalsIgnoreCase(si)){
                   si_ylList.add(si);
                   flag = true;
                   break;
               }
           }
           if(!flag)
               si_yl_except.add(yl);
           flag = false;
       }
       for(String sy : syTableNames){
           for(String si : sicp3TableNames){
               if(sy.equalsIgnoreCase(si)){
                   si_syList.add(si);
                   flag = true;
                   break;
               }
           }
           if(!flag)
               si_sy_except.add(sy);
           flag = false;
       }
       List<Map> mapList = new ArrayList<>();
       List<String> noMatch = new ArrayList<>();
       Map<String,List> map = new HashMap<>();
       Map<String,List> noMatchMap = new HashMap<>();
       for(String tableName : si_ylList){
           List<String> si_list = getTableStruct(NOW,tableName);
           List<String> yl_list = getTableStruct(YL,tableName);
           List<String> res = compareList(yl_list,si_list,noMatch);
           if(res.size() != 0)
               map.put(tableName,res);
           if(noMatch.size() != 0 ){
               List<String> cpNoMatch = new ArrayList<>();
               cpNoMatch.addAll(noMatch);
               noMatchMap.put(tableName,cpNoMatch);
           }
           noMatch.clear();
       }
       for(String tableName : si_syList){
           List<String> si_list = getTableStruct(NOW,tableName);
           List<String> sy_list = getTableStruct(SY,tableName);
           List<String> res = compareList(sy_list,si_list,noMatch);
           if(res.size() != 0)
               map.put(tableName,res);
           if(noMatch.size() != 0 ){
               List<String> cpNoMatch = new ArrayList<>();
               cpNoMatch.addAll(noMatch);
               noMatchMap.put(tableName,cpNoMatch);
           }
           noMatch.clear();
       }
       try{
           File file = new File("D:\\ylzProjectDocument\\columnCompareRes.txt");
           if(!file.exists())
               file.createNewFile();
           FileWriter fileWriter = new FileWriter(file.getAbsoluteFile());
           BufferedWriter bufferedWriter = new BufferedWriter(fileWriter);
           for(Map.Entry<String,List> entry : map.entrySet()){
               String tableName = entry.getKey();
               List<String> res = entry.getValue();
               bufferedWriter.write(String.format("表%s的差异字段如下：\n\n",tableName));
               for(String line : res){
                   bufferedWriter.write(line);
                   bufferedWriter.write("\n");
               }
               bufferedWriter.write("\n\n");
           }
           for(Map.Entry<String,List> entry : noMatchMap.entrySet()){
               String tableName = entry.getKey();
               List<String> matchNo = entry.getValue();
               bufferedWriter.write(String.format("表%s未匹配到的字段如下：\n",tableName));
               for(String line : matchNo){
                   bufferedWriter.write(line);
                   bufferedWriter.write("\n");
               }
               bufferedWriter.write(String.format("sicp3库中%s表的字段如下：\n",tableName));
               List<String> struct = getTableStruct(NOW,tableName);
               for(String row : struct){
                   bufferedWriter.write(row);
                   bufferedWriter.write("\n");
               }
               bufferedWriter.write("\n\n");
           }

           bufferedWriter.write("养老数据库中未匹配的表如下:\n");
           for(String yl : si_yl_except){
               bufferedWriter.write(yl);
               bufferedWriter.write("\n");
           }
           bufferedWriter.write("失业数据库中未匹配的表如下:\n");
           for(String sy : si_sy_except){
               bufferedWriter.write(sy);
               bufferedWriter.write("\n");
           }
           bufferedWriter.close();
       } catch (IOException e) {
           e.printStackTrace();
       }
   }

   private void write2File(String fileName,List<String> tableList,String userName){
        try{
            File file = new File(fileName);
            if(!file.exists())
                file.createNewFile();
            FileWriter fileWriter = new FileWriter(file.getAbsoluteFile());
            BufferedWriter bufferedWriter = new BufferedWriter(fileWriter);
            for(String table:tableList){
                bufferedWriter.write(String.format("%s表结构如下：\n",table));
                List<String> struct = getTableStruct(userName,table);
                if(struct.size() == 0 || table.equalsIgnoreCase("aa10") )
                    struct = getTableStruct(userName,table);
                for(String cloumn : struct){
                    bufferedWriter.write(cloumn);
                    bufferedWriter.write("\n");
                }
                bufferedWriter.write("\n");
            }
            bufferedWriter.flush();
            bufferedWriter.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
  }

  public void main2(){
        String fileName = "D:\\ylzProjectDocument\\福州社保\\网报前置库表结构.txt";
        String fileName2 = "D:\\ylzProjectDocument\\福州社保\\网报sicp3表结构.txt";
        write2File(fileName,tableList,YL);
        write2File(fileName2,nowTableList,NOW);
  }

   private List<String> compareList(List<String> list1, List<String> list2,List<String> noMatch){
        List<String> res = new ArrayList<>();
//        List<String> noMatch = new ArrayList<>();
        for(String iter : list1){
            String[] split = iter.split("_");
            String clu = split[0];
            String comment = split[1];
            Boolean flag = false;
            for(String iter2 : list2){
                String[] split2 = iter2.split("_");
                String clu2 = split2[0];
                String comment2 = split2[1];
                if(clu.equalsIgnoreCase(clu2)){
                    flag = true;
                    break;
                }
                else if(comment.equalsIgnoreCase(comment2)){
                    res.add(iter + "   "+ iter2);
                    flag = true;
                }
                else if(comment.contains(comment2) || comment2.contains(comment)){
                    System.out.println(iter+"   "+ iter2);
                }
            }
            if(!flag){
                noMatch.add(iter);
            }
        }
        return res;
   }

   private Map<String,String> getTableComment(String userName,List<String> tableNames){
        Connection conn = getConnection(userName);
        Map<String,String> map = new HashMap<>();
       try{
           String sql = "select * from user_tab_comments";
           Statement st = conn.createStatement();
           ResultSet res = st.executeQuery(sql);
           while (res.next()){
               String tableName = res.getString("TABLE_NAME");
               String comment = res.getString("COMMENTS");
               Boolean flag = false;
               for(String table : tableNames){
                   if(table.equalsIgnoreCase(tableName)){
                       flag = true;
                       break;
                   }
               }
               if(flag)
                   map.put(tableName,comment);
           }
           res.close();
           st.close();
           conn.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return map;
   }

   private void write2File(String fileName,Map<String,String> map){
       File file = new File(fileName);
       try{
           if(!file.exists())
               file.createNewFile();
           FileWriter fileWriter = new FileWriter(file.getAbsoluteFile());
           BufferedWriter bufferedWriter = new BufferedWriter(fileWriter);
//           for(String iter : list){
           for(Map.Entry<String,String> entry : map.entrySet()){
               String tableName = entry.getKey();
               String comment = entry.getValue();
               bufferedWriter.write(tableName);
               bufferedWriter.write("\t");
               if(comment != null)
                   bufferedWriter.write(comment);
               bufferedWriter.write("\n");
           }
           bufferedWriter.flush();
           bufferedWriter.close();
       } catch (IOException e) {
           e.printStackTrace();
       }
   }

   private void main3(){
        Connection conn = getConnection(NOW);
        try{
            Statement st = conn.createStatement();
            String sql = "select aac001 from ac01";
            String sql2 = "select aab001 from ab01a1 where aab001>10000";
            String[] aac001s = {"6213543698","6170715178","6290830817","6171803810","6215961820","6264361758","6212030944","6171726150","6293381835","6256227082","6283750601","6216331192","6197353243","6170590782","6170342370","6170643558","6170131618","6168943443","6173481420","6216123921","6179814917","6216098757","6188715257","6229538452","6171414316","6174561720","6215683572","6165762327","6246090499","6204123451","6218072985","6196838517","6188446584","6193199531","6209849260","6194761911","6165309005","6167283486","6304584170","6304584370","6304584223","6203616411","6180090841","6173333534","6304822100","6168830726","6207960391","6176562288"};
            String[] aab001s = {"209176021","209491278","211809502","211858960","208908182","211774541","210365085","210540128","209695064","210508790","208793585","209001998","210537836","209603721","209589241","209593200","210532636","210427434","210351437","211868874","211868874","211094075","210658274","210976318","211013260","211013260","211087209","210658274","211065181","210914410","211065181","211056846","208816098","208816098","208792113","211038298","210957216","211087209","210936852","210936852","210936852","210914410","210914410","211102229","211521667","211676737","211676737","211790769","211790769","211790769","213318407","213318407","208821309","208848989","211675665","211675665","211958285","208790527","209090469","209386153","209386153","209386153","209386153","209386153","209386153","209386153","209386153","209395061","209397228","209397228","209397228","209416207","209386153","209386153","209386153","209386153","209386153","209386153","209386153","209386153"};
            ResultSet res1 = st.executeQuery(sql2);
            List<Long> aab001_src = new ArrayList<>();
            while (res1.next()){
                Long aab001Src = res1.getLong("aab001");
                aab001_src.add(aab001Src);
            }
            for(int i = 0;i<aac001s.length;i++){
                Long aab001 = Long.parseLong(aab001s[i]);
                st.executeUpdate("update ab01a1temp set aab001 = "+aab001 + " where aab001 = "+aab001_src.get(i));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
   }
   public static void main(String[] args){
        Tools tools = new Tools();
        tools.main3();
//        List<String> ylList = tools.getTableNames(YL);
//        List<String> gsList = tools.getTableNames(Tools.SY);
//        Map<String,String> ylMap = tools.getTableComment(YL,ylList);
//        Map<String,String> syMap = tools.getTableComment(SY,gsList);
//        String file1 = "D:\\ylzProjectDocument\\福州社保\\养老前置库表.txt";
//        String file2 = "D:\\ylzProjectDocument\\福州社保\\失业前置库表.txt";
//        tools.write2File(file1,ylMap);
//        tools.write2File(file2,syMap);
//        tools.main();
    }
}
