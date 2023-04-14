package cn.luischen.utils;

import java.io.*;
import java.util.*;

/**
 * 一个本地文件系统读写操作的工具类
 * @author ket
 * @since 2021-11-24
 */
public final class FileUtils {

    /**
     * 获取文件名的简单名字，去掉后缀
     */
    public static String getFileSimpleName(File file) {
        return file.getName().split("\\.")[0];
    }

    public static boolean exists(String path) {
        return exists(new File(path));
    }

    public static boolean exists(File file) {
        return file.exists();
    }

    //----------------------------------------------------------------------------------------------
    //                                          Read File
    //----------------------------------------------------------------------------------------------

    /**
     * 加载resource目录中的文件
     *
     * @param name 文件名
     * @return 按行封装成list
     */
    public static List<String> loadResourceLine(String name) {
        InputStream inputStream = ClassLoader.getSystemResourceAsStream(name);
        Objects.requireNonNull(inputStream);
        ArrayList<String> list = new ArrayList<>();
        try (BufferedReader reader =
                     new BufferedReader(new InputStreamReader(inputStream))) {
            String line;
            while ((line = reader.readLine()) != null) {
                list.add(line);
            }
        } catch (IOException e) {
            throw new RuntimeException("Error parsing file.", e);
        }
        return list;
    }

    /**
     * 返回文件每一行组成的list
     *
     * @param file 文件路径
     */
    public static List<String> loadFileLine(File file) {
        ArrayList<String> list = new ArrayList<>();
        try (BufferedReader reader =
                     new BufferedReader(new InputStreamReader(new FileInputStream(file)))) {
            String line;
            while ((line = reader.readLine()) != null) {
                list.add(line);
            }
        } catch (IOException e) {
            throw new RuntimeException("Error parsing file.", e);
        }
        return list;
    }

    /**
     * 返回文件每一行组成的list
     *
     * @param path 文件路径
     */
    public static List<String> loadFileLine(String path) {
        return loadFileLine(new File(path));
    }

    /**
     * 遍历获取文件路径下所有指定名字结尾的文件
     *
     * @param file 文件夹路径
     * @param suffix  结尾名
     * @return 文件集合
     */
    public static List<File> loadEndWithFileList(File file, String suffix) {
        List<File> list = new ArrayList<>();
        if (file.isFile()) {
            if (file.getAbsolutePath().endsWith(suffix)) {
                list.add(file);
                return list;
            }
        }

        File[] listFiles = file.listFiles(new FileFilter() {
            @Override
            public boolean accept(File pathname) {
                return pathname.isDirectory() || pathname.getAbsolutePath().endsWith(suffix);
            }
        });
        for (File f : Objects.requireNonNull(listFiles)) {
            if (f.isFile()) {
                list.add(f);
            } else {
                list.addAll(loadEndWithFileList(f, suffix));
            }
        }
        return list;
    }

    /**
     * 遍历获取文件路径下所有指定名字结尾的文件
     *
     * @param path 文件夹路径
     * @param suffix  结尾名
     * @return 文件集合
     */
    public static List<File> loadEndWithFileList(String path, String suffix) {
        return loadEndWithFileList(new File(path), suffix);
    }

    /**
     * 遍历获取文件路径下所有指定名字结尾的文件内容集合
     *
     * @param file 文件夹
     * @param suffix  结尾名
     * @return 文件内容集合
     */
    public static List<List<String>> loadEndWithFileContentList(File file, String suffix) {
        List<List<String>> list = new ArrayList<>();
        List<File> fileList = loadEndWithFileList(file, suffix);
        for (File f : fileList) {
            list.add(loadFileLine(f));
        }
        return list;
    }

    /**
     * 遍历获取文件路径下所有指定名字结尾的文件内容集合
     *
     * @param path 文件夹路径
     * @param suffix  结尾名
     * @return 文件内容集合
     */
    public static List<List<String>> loadEndWithFileContentList(String path, String suffix) {
        return loadEndWithFileContentList(new File(path), suffix);
    }



    /**
     * 将文件名和对应的文件内容写入给定的MAP中
     *
     * @param file 文件路径
     * @param map  给定的map集合
     */
    public static Map<String, List<String>> loadFileNameAndContentMap(File file, Map<String, List<String>> map) {
        map.put(getFileSimpleName(file), loadFileLine(file));
        return map;
    }

    /**
     * 将文件名和对应的文件内容写入给定的MAP中
     *
     * @param path 文件路径
     * @param map  给定的map集合
     */
    public static Map<String, List<String>> loadFileNameAndContentMap(String path, Map<String, List<String>> map) {
        return loadFileNameAndContentMap(new File(path), map);
    }

    /**
     * 将文件名和对应的文件内容写入一个新的MAP中
     *
     * @param file 文件路径
     */
    public static Map<String, List<String>> loadFileNameAndContentMap(File file) {
        return loadFileNameAndContentMap(file, new HashMap<>());
    }

    /**
     * 将文件名和对应的文件内容写入一个新的MAP中
     *
     * @param path 文件路径
     */
    public static Map<String, List<String>> loadFileNameAndContentMap(String path) {
        return loadFileNameAndContentMap(new File(path));
    }

    /**
     * 遍历获取文件路径下所有指定名字结尾的文件MAP集合，K为文件名，V为文件内容
     *
     * @param file 文件夹
     * @param suffix  结尾名
     * @return 文件名和内容对应Map
     */
    public static Map<String, List<String>> loadEndWithFileContentMap(File file, String suffix) {
        HashMap<String, List<String>> map = new HashMap<>();
        List<File> fileList = loadEndWithFileList(file, suffix);
        for (File f : fileList) {
            map.put(getFileSimpleName(f), loadFileLine(f));
        }
        return map;
    }

    /**
     * 遍历获取文件路径下所有指定名字结尾的文件MAP集合，K为文件名，V为文件内容
     *
     * @param path 文件夹路径
     * @param suffix  结尾名
     * @return 文件名和内容对应Map
     */
    public static Map<String, List<String>> loadEndWithFileContentMap(String path, String suffix) {
        return loadEndWithFileContentMap(new File(path), suffix);
    }

    //----------------------------------------------------------------------------------------------
    //                                          Write File
    //----------------------------------------------------------------------------------------------

    /**
     * 写数据到文件，没有则创建
     * @param file  目标文件
     * @param data  数据
     */
    public static void writeFile(File file, String data) {
        try (BufferedWriter writer =
                     new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file)))) {
            writer.write(data);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 写数据到文件，没有则创建
     * @param file  目标文件
     * @param data  数据集合
     */
    public static void writeFile(File file, List<String> data) {
        writeFile(file,String.join("\n",data));
    }

    /**
     * 写数据到文件，没有则创建
     * @param path  路径
     * @param data  数据
     */
    public static void writeFile(String path, String data) {
        writeFile(new File(path), data);
    }

    /**
     * 写数据到文件，没有则创建
     * @param path  路径
     * @param data  数据
     */
    public static void writeFile(String path, List<String> data){
        writeFile(new File(path),data);
    }

    private FileUtils() {

    }
}