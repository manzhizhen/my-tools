/**
 * 
 */
package com.sfpay.tools.database;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.sfpay.tools.util.StringUtils;

/**
 * 类说明：将建表语句转化成JAVA对象
 * 类描述：将建表语句转化成JAVA对象
 * 
 * 使用方法如下：
 * 1.将pdm中表格属性中的“预览”中的建表(create table)和备注(comment on)语句部分拷贝到项目中的create_table_sql.txt文件中
 * 2.运行main方法即可，当然你也可以引入外面的建表sql文件
 * 
 * 优点 1.不需要数据库，直接通过建表SQL语句即可打印出Mapper文件中通用增删改查内容，
 * 		  而且建表SQL语句可以直接从pdm中表格“预览Preview”拷贝出来。
 *     2.不像MyBatis自动生成脚本插件那么有侵入性（它直接在项目中生成文件，所以多人开发需谨慎使用），
 *       我们这个直接在控制台输出，你可以将需要的文本拷贝到你自己的项目中来增加开发效率。
 *     3.源代码就一个java文件，可以自己修改或新增符合自己风格的文本输出。
 * 
 * @author 625288 易振强
 * 2014-11-12
 * version 1.0.1
 */
public class CreateTableSqlToJavaClass {
	// COLUMN COMMENT语句的正则
	private static final String COLUMN_COMMENT_PATTERN = "\\s*COMMENT\\s+ON\\s+COLUMN\\s+(\\S+)\\s+IS\\s+'(.*)'\\s*;";
	// TABLE COMMENT语句的正则
	private static final String TABLE_COMMENT_PATTERN = "\\s*COMMENT\\s+ON\\s+TABLE\\s+(\\S+)\\s+IS\\s+'(.*)'\\s*;";
	
	public static void main(String[] args) {
		// 这里是直接引用项目中的createsql.txt，当然你也可以引用其他地方的
		printJavaClass("src/main/resources/create_table_sql1.txt");
		// SetGet方法就交给eclipse自动生成咯！
	}
	
	/**
	 * 将写有建表语句的sql文件转换成Java类后打印出来
	 * @param sqlFile
	 */
	public static void printJavaClass(String sqlFile) {
		BufferedReader reader = null;
		try {
			File createTableSqlFile = new File(sqlFile);
			if(!createTableSqlFile.isFile()) {
				System.out.println("SQL文件不存在：" + createTableSqlFile.getAbsolutePath());
				return ;
			}
			
			reader = new BufferedReader(new InputStreamReader(
					new FileInputStream(createTableSqlFile)));
			
			String line = null;
			StringBuilder clazzComment = new StringBuilder();	// 类备注
			String tableName = null;	// 表名
			String clazzStart = null;	// 类的开头
			String clazzEnd = "}";		// 类的结尾
			StringBuilder clazzStr = null;
			boolean find = false;		// 是否找到了建表语句
			// 为了将建表的comment作为对应Java类属性的注解，我们需要先将表字段的解析结果放入Map中
			Map<String, String> tableMap = new LinkedHashMap<String, String>();
			String separator = System.getProperty("line.separator");
			
			// 开始解析create table 建表语句
			while(true) {
				line = reader.readLine();
				if(line == null) {
					break ;
				}
				
				line = line.trim().toUpperCase();
				List<String> strList = Arrays.asList(line.split("\\s+"));
				
				// 过滤掉注解
				if(strList.get(0).trim().startsWith("--")) {
					continue ;
				}
				
				// 包含这两个关键字就是我们要找的建表语句了
				if(!find && strList.contains("CREATE") && strList.contains("TABLE")) {
					clazzStr = new StringBuilder();
					clazzStr.append("public class ");
					clazzStr.append(getClazzName(strList.get(2)));	// 将表名转换成类名
					clazzStr.append(" {");
					clazzStr.append(separator);
					
					tableName = strList.get(2);
					clazzStart = clazzStr.toString();
					
					find = true;
					continue ;
				}
				
				// 在 CREATE TABLE 语句下面找建字段的语句
				if(find && strList.size() >= 2 && !strList.contains("CONSTRAINT")){
					clazzStr = new StringBuilder();
					clazzStr.append("\tprivate ");
					// 如果类型结尾有逗号，则去掉
					String type = strList.get(1);
					type = type.endsWith(",") ? type.substring(0, type.length() - 1) : type;
					clazzStr.append(getClazzFieldTypeName(type));	// 将表字段类型转换成Java类型
					clazzStr.append(" ");
					clazzStr.append(getClazzFieldName(strList.get(0)));		// 将表字段名称转换成Java类属性名称
					clazzStr.append(";");
					clazzStr.append(separator);
					
					tableMap.put(strList.get(0), clazzStr.toString());
				} 
				
				// ); 一般是建表语句的结束符
				if(line.trim().matches("\\).*;")) {
					break ;
				} else if(line.trim().equals(")")) {
					line = line.trim() + reader.readLine();
					if(line.trim().matches("\\).*;")) {
						break;
					}
				}
			}
			
			// 一般建表语句下面跟的是建备注comment的语句
			
			// 将备注解析到该Map中，key为字段名称，value为备注的描述
			Map<String, String> commentMap = new LinkedHashMap<String, String>();
			Pattern columnPattern = Pattern.compile(COLUMN_COMMENT_PATTERN);
			Pattern tablePattern = Pattern.compile(TABLE_COMMENT_PATTERN);			
			Matcher columnMatcher = null;	// 列备注匹配器
			Matcher tableMatcher = null;	// 表备注匹配器
			while(true) {
				line = reader.readLine();
				if(line == null) {
					break ;
				}
				
				if(StringUtils.isBlank(line)) {
					continue ;
				}
				
				// 一条完整备注语句有可能不在一行，但肯定以;结尾
				String temp = null;
				while(!line.contains(";")) {
					temp = reader.readLine();
					if(temp == null) {
						break ;
					}
					line = line.trim() + " " + temp.trim();
				}
				
				line = line.toUpperCase();
				columnMatcher = columnPattern.matcher(line);
				// 如果能匹配，说明是列备注
				if(columnMatcher.matches()) {
					commentMap.put(columnMatcher.group(1), columnMatcher.group(2).trim());	// 列明和列备注放入map中
					continue ;
				}
				
				tableMatcher = tablePattern.matcher(line);
				// 如果能匹配，说明是表备注
				if(tableMatcher.matches()) {
					clazzComment.append("/**" + separator);
					clazzComment.append(" * 类说明：" + tableMatcher.group(2) + separator);
					clazzComment.append(" * 类描述：" + tableMatcher.group(2) + separator);
					clazzComment.append(" * @author 顺丰攻城狮" + separator);
					clazzComment.append(" */" + separator);
					continue ;
				}
				
			}
			
			StringBuilder resultStr = new StringBuilder();
			resultStr.append(clazzComment.toString());	// 加入类注释
			resultStr.append(clazzStart);				// 加入类开头
			// 加入类属性
			for(Map.Entry<String, String> entry : tableMap.entrySet()) {
				if(commentMap.containsKey(tableName + "." + entry.getKey())) {
					resultStr.append("\t// " + commentMap.get(tableName + "." + entry.getKey()) + separator);
				}
				
				resultStr.append(entry.getValue());
			}
			resultStr.append(clazzEnd);	// 加入类结尾
			
			System.out.println(resultStr.toString());
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if(reader != null) {
				try {
					reader.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}
	
	/**
	 * 将表名转化成Java类名称
	 * @param sqlName
	 * @return
	 */
	public static String getClazzName(String sqlName) {
		return getClazzStr(sqlName);
	}
	
	/**
	 * 将表字段名称转化成Java类属性名称
	 * @param sqlName
	 * @return
	 */
	public static String getClazzFieldName(String sqlName) {
		String result = getClazzStr(sqlName);
		
		if(StringUtils.isBlank(result)) {
			return result;
		}
		
		return result.toLowerCase().charAt(0) + result.substring(1);
	}
	
	/**
	 * 将Oracle 11g 数据类型转换成Java类型
	 * @param type
	 * @return
	 */
	public static String getClazzFieldTypeName(String type) {
		if(StringUtils.isBlank(type)) {
			return "UNDEFINE";
		}
		
		type = type.trim().toUpperCase();
		
		if(type.contains("CHAR") || type.contains("CLOB")) {
			return "String";
			
		} else if(type.contains("DATE") || type.contains("TIMESTAMP")) {
			return "Date";
			
		} else if(type.contains("NUMBER") && !type.contains(",")) {
			int leftIndex = type.indexOf("(");
			if(leftIndex < 0) {
				return "Long";
			}
			
			int length = Integer.valueOf(type.substring(leftIndex + 1, type.indexOf(")")));
			if(length <= 9) {
				return "Integer";
			} else {
				return "Long";
			}
			
		} else if(type.contains("NUMBER") && type.contains(",")) {
			return "Double";
			
		} else if(type.contains("FLOAT") || type.contains("BINARY_FLOAT")) {
			return "Float";
			
		} else if(type.contains("LONG")) {
			return "Long";
			
		} else if(type.contains("BINARY_DOUBLE")) {
			return "Double";
			
		} else if(type.contains("BLOB")) {
			return "byte[]";
		} else {
			System.out.println("未知类型,getClazzFieldTypeName转化失败:" + type);
		}
		
		return String.format("UNDEFINE(%s)", type);
	}
	
	private static String getClazzStr(String sqlName) {
		if(StringUtils.isBlank(sqlName)) {
			return "";
		}
		
		if(!sqlName.contains("_")) {
			return sqlName.toLowerCase();
		}
		
		StringBuilder result = new StringBuilder();
		String[] arrayStr = sqlName.split("_");
		for(String str : arrayStr) {
			result.append(str.toUpperCase().charAt(0) + str.toLowerCase().substring(1));
		}
		
		return result.toString();
	}
}
