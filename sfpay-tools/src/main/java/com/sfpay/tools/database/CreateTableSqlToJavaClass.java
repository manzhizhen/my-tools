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
import java.util.LinkedList;
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
	// 需将所有SQL语句的单引号（默认值或备注）中的东西先提取出来
	private static final String SINGLE_QUOTES_REG = "'[^']*'";
	// 单引号内容替换后的标记
	private static final String SINGLE_QUOTES_FLAG = "'#'";
	// 创建Table表格正则
	private static final String TABLE_REG = "\\s*CREATE\\s+TABLE\\s+([\\w\\.]+)\\s*\\((.*)";
	// COLUMN COMMENT语句的正则
	private static final String COLUMN_COMMENT_PATTERN = "\\s*COMMENT\\s+ON\\s+COLUMN\\s+(\\S+)\\s+IS\\s+'(.*)'\\s*";
	// TABLE COMMENT语句的正则
	private static final String TABLE_COMMENT_PATTERN = "\\s*COMMENT\\s+ON\\s+TABLE\\s+(\\S+)\\s+IS\\s+'(.*)'\\s*";
	
	public static void main(String[] args) {
		// 这里是直接引用项目中的createsql.txt，当然你也可以引用其他地方的
		printJavaClass("src/main/resources/create_table_sql.txt");
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
			
			// 先读取整个SQL文件
			String line = null;
			StringBuilder sqlBuilder = new StringBuilder();
			while(true) {
				line = reader.readLine();
				if(line == null) {
					break ;
				}
				
				if(StringUtils.isBlank(line)) {
					continue ;
				}
				
				line = line.trim();
				
				if(line.startsWith("--") || line.startsWith("/*")) 
					continue;
				
				sqlBuilder.append(line.trim() + " ");
			}
			
			// 将引号中的内容提取出来（默认值或备注）
			List<String> quotesContextList = new LinkedList<String>();
			String totalSql = sqlBuilder.toString().trim();
			int index = 0;
			while(true) {
				int start = totalSql.indexOf('\'', index);
				if(start < 0) {
					break ;
				}
				int end = totalSql.indexOf('\'', start + 1);
				if(end < 0) 
					throw new IllegalStateException("我靠，这SQL语句的单引号怎么是单数！？");
				
				quotesContextList.add(totalSql.substring(start + 1, end));
				
				index = end + 1;
			}
			
			// 将单引号中内容替换，以免里面内容干扰SQL解析
			totalSql = totalSql.replaceAll(SINGLE_QUOTES_REG, SINGLE_QUOTES_FLAG);
			// 为了分析简单，全大写
			totalSql = totalSql.toUpperCase();
			// 以分号结束的SQL语句
			List<String> sqls = Arrays.asList(totalSql.split(";"));
			
			StringBuilder clazzComment = new StringBuilder();	// 类备注
			String tableName = null;	// 表名
			String clazzStart = null;	// 类的开头
			String clazzEnd = "}";		// 类的结尾
			StringBuilder clazzStr = null;
			
			String separator = System.getProperty("line.separator");
			// 为了将建表的comment作为对应Java类属性的注解，我们需要先将表字段的解析结果放入Map中
			Pattern columnPattern = Pattern.compile(COLUMN_COMMENT_PATTERN);
			Pattern tablePattern = Pattern.compile(TABLE_COMMENT_PATTERN);	
			
			Map<String, String> tableMap = new LinkedHashMap<String, String>();
			// 将备注解析到该Map中，key为字段名称，value为备注的描述
			Map<String, String> commentMap = new LinkedHashMap<String, String>();
			
			for(String sql : sqls) {
				// 如果是建表语句
				if(sql.matches(TABLE_REG)) {
					Pattern pattern = Pattern.compile(TABLE_REG);
					Matcher matcher = pattern.matcher(sql);
					matcher.matches();
					// 表名
					tableName = matcher.group(1).trim();

					clazzStr = new StringBuilder();
					clazzStr.append("public class ");
					clazzStr.append(getClazzName(tableName));	// 将表名转换成类名
					clazzStr.append(" {");
					clazzStr.append(separator);
					clazzStart = clazzStr.toString();
					
					// 建表后面的语句
					String columnsSql = matcher.group(2).trim();
					List<String> columnSql = Arrays.asList(columnsSql.split(","));
					for(String column : columnSql) {
						column = column.trim();
						
						String[] strList = column.split("\\s+");
						// 说明是建字段的语句
						if(!strList[0].equals("CONSTRAINT")) {
							clazzStr = new StringBuilder();
							clazzStr.append("\tprivate ");
							// 如果类型结尾有逗号，则去掉
							String type = strList[1];
							type = type.endsWith(",") ? type.substring(0, type.length() - 1) : type;
							clazzStr.append(getClazzFieldTypeName(type));	// 将表字段类型转换成Java类型
							clazzStr.append(" ");
							clazzStr.append(getClazzFieldName(strList[0]));		// 将表字段名称转换成Java类属性名称
							clazzStr.append(";");
							clazzStr.append(separator);
							
							tableMap.put(strList[0], clazzStr.toString());
						} 
					}
					
					// 去掉建表语句中的默认值（引号部分），因为默认值这东西对生成Class没什么用
					while(sql.contains(SINGLE_QUOTES_FLAG)) {
						// 将第一个替换成另一个字符串
						sql = sql.replaceFirst(SINGLE_QUOTES_FLAG, "'&'");
						quotesContextList.remove(0);
					}
				
				// 如果是列备注语句
				} else if(sql.matches(COLUMN_COMMENT_PATTERN)) {
					Matcher columnMatcher = columnPattern.matcher(sql);
					columnMatcher.matches();
					commentMap.put(columnMatcher.group(1), quotesContextList.get(0).trim());	// 列明和列备注放入map中
					// 用一个去掉一个，所以每次取出第一个就行了
					quotesContextList.remove(0);
					
				// 如果是表备注语句
				} else if(sql.matches(TABLE_COMMENT_PATTERN)) {
					Matcher tableMatcher = tablePattern.matcher(sql);
					tableMatcher.matches();
					clazzComment.append("/**" + separator);
					clazzComment.append(" * 类说明：" + quotesContextList.get(0).trim() + separator);
					clazzComment.append(" * 类描述：" + quotesContextList.get(0).trim() + separator);
					clazzComment.append(" * @author 顺丰攻城狮" + separator);
					clazzComment.append(" */" + separator);
					// 用一个去掉一个，所以每次取出第一个就行了
					quotesContextList.remove(0);
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
		
		if(sqlName.contains(".")) {
			sqlName = sqlName.substring(sqlName.lastIndexOf(".") + 1, sqlName.length());
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
