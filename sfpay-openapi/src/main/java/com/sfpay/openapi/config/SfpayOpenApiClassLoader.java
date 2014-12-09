package com.sfpay.openapi.config;

//import java.io.File;
//import java.io.FileInputStream;
//import java.io.IOException;
//import java.net.URL;
//import java.util.Arrays;
//import java.util.concurrent.ConcurrentHashMap;
//import java.util.concurrent.ConcurrentMap;

//import org.springframework.beans.factory.annotation.Value;
//import org.springframework.stereotype.Component;

/**
 * OpenApi的类加载器 用来动态加载服务API接口
 * 
 * @author 625288 2014年10月4日
 */
//@Component
@Deprecated
public class SfpayOpenApiClassLoader extends ClassLoader {
/*	@Value(value="${CLASS_FILE_PATH}")
	private String classFilePath;
	private int CLASS_BYTE_SIZE = 1024 * 100;
	
	// 完整类名（包名+类名）和类对象的映射关系
	@SuppressWarnings("rawtypes")
	private ConcurrentMap<String, Class> classMap = new ConcurrentHashMap<String, Class>();
	
	@Override
	public Class<?> loadClass(String name) throws ClassNotFoundException {
		@SuppressWarnings("rawtypes")
		Class clazz = classMap.get(name);
		if(clazz != null) {
			return clazz;
		}
		
		clazz = ObjectSwitchTool.getBeseClass(name);
		if(clazz != null) {
			classMap.putIfAbsent(name, clazz);
			return clazz;
		}
		
		try {
			clazz = Thread.currentThread().getContextClassLoader().loadClass(name);
		} catch (ClassNotFoundException e) {
			try {
				clazz = getSystemClassLoader().loadClass(name);
				classMap.putIfAbsent(name, clazz);
			} catch (Exception e1) {
				try {
					clazz = super.loadClass(name);
					classMap.putIfAbsent(name, clazz);
				} catch (Exception e2) {
					if(clazz == null) {
						clazz = findClass(name);
						if(clazz != null) {
							classMap.putIfAbsent(name, clazz);
						}
					}
				}
			}
		}
		
		return clazz;
	}

	@Override
	protected Class<?> findClass(String name) throws ClassNotFoundException {
		byte[] b;
		try {
			b = loadClassData(name);
		} catch (IOException e) {
			e.printStackTrace();
			throw new ClassNotFoundException("findClass失败：class文件加载失败", e);
		}
		return defineClass(name, b, 0, b.length);
	}

	*//**
	 * 从本地载入class文件数据
	 * 
	 * @param name
	 * @return
	 * @throws IOException 
	 *//*

	private byte[] loadClassData(String name) throws IOException {
		// 将类全名转换为相对文件路径
		StringBuilder path = new StringBuilder(name.replace(".", File.separator));
		path.append(".class");
		File file = new File(classFilePath, path.toString());
		
		FileInputStream fileInputStream = null;
		try {
			fileInputStream = new FileInputStream(file);
			byte[] bytes = new byte[CLASS_BYTE_SIZE];
			int readSize = fileInputStream.read(bytes);
			if(readSize >= CLASS_BYTE_SIZE) {
				throw new IOException("从本地载入class文件数据失败：class文件尺寸超过限制!");
			}
			
			return Arrays.copyOf(bytes, readSize);
		} finally {
			if(fileInputStream != null) {
				fileInputStream.close();
			}
		}
	}
	
    public URL findResource(String name) {
    	return null;
    }

	protected void setClassFilePath(String classFilePath) {
		this.classFilePath = classFilePath;
	}

	*//**
	 * 清空class缓存
	 *//*
	public void cleanClassCache() {
		classMap.clear();
	}
	
	public static void main(String[] args) {
		try {
			SfpayOpenApiClassLoader loader = new SfpayOpenApiClassLoader();
			loader.setClassFilePath("d:/classes");
			Class clazz = loader.loadClass("com.sfpay.web.service.TestService");
			System.out.println(clazz.getDeclaredMethods().length);

			Thread.sleep(1000);
			
			System.out.println("再次加载");
			loader.cleanClassCache();
			clazz = loader.loadClass("com.sfpay.web.service.TestService");
			System.out.println(clazz.getDeclaredMethods().length);
			
			
			
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		
	}*/

}
