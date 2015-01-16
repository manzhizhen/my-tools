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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.sfpay.tools.util.StringUtils;


/**
 * 类说明：将建表语句转化成Mapper的xml文件
 * 
 * 类描述：将建表语句转化成Mapper的xml文件
 * 优点 1.不需要数据库，直接通过建表SQL语句即可打印出Mapper文件中通用增删改查内容，
 * 		  而且建表SQL语句可以直接从pdm中表格“预览Preview”拷贝出来。
 *     2.不像MyBatis自动生成脚本插件那么有侵入性（它直接在项目中生成文件，所以多人开发需谨慎使用），
 *       我们这个直接在控制台输出，你可以将需要的文本拷贝到你自己的项目中来增加开发效率。
 *     3.源代码就一个java文件，可以自己修改或新增符合自己风格的文本输出。
 * 
 * @author 625288 易振强 2014-11-12
 */
public class CreateTableSqlToMapper {
	private static final String SEPARATOR = System.getProperty("line.separator");
	// 创建主键语句的正则
	private static final String CONSTRAINT_PRIMARY_KEY = "\\s*CONSTRAINT\\s+\\S+\\s+PRIMARY\\s+KEY.*\\((\\S+)\\).*";
	// 排版时换行字符数
	private static final int CHAR_SEPARATOR_NUM = 90;
	
	
	public static void main(String[] args) {
		// 这里是直接引用项目中的createsql.txt，当然你也可以引用其他地方的
		printMapperXml("src/main/resources/create_table_sql2.txt");
	}
	
	/**
	 * 从包含建表语句的sql文件中打印出MyBatis的Mapper文件中需要用到的关键动态SQL语句
	 * @param sqlFile
	 */
	public static void printMapperXml(String sqlFile) {
		BufferedReader reader = null;
		try {
			File createTableSqlFile = new File(sqlFile);
			if(!createTableSqlFile.isFile()) {
				System.out.println("SQL文件不存在：" + sqlFile);
				return ;
			}
			
			reader = new BufferedReader(new InputStreamReader(
					new FileInputStream(createTableSqlFile)));

			String line = null;
			String tableName = null;
			
			boolean find = false;		// 是否找到了建表语句
			List<MapperData> mapperDataList = new ArrayList<MapperData>();
			Pattern primaryKeyPattern = Pattern.compile(CONSTRAINT_PRIMARY_KEY);			
			Matcher primaryKeyMatcher = null;	// 主键匹配器
			// 解析建表语句
			while (true) {
				line = reader.readLine();
				if (line == null) {
					break;
				}

				line = line.trim().toUpperCase();
				List<String> strList = Arrays.asList(line.split("\\s+"));

				if (!find && strList.contains("CREATE") && strList.contains("TABLE")) {
					tableName = strList.get(2).toUpperCase();
					find = true;
					continue;
				}
				
				primaryKeyMatcher = primaryKeyPattern.matcher(line);
				// 在 CREATE TABLE 语句下面找建字段的语句
				if (find && strList.size() >= 2 && !primaryKeyMatcher.matches()) {
					mapperDataList.add(new MapperData(strList.get(0), CreateTableSqlToJavaClass.
							getClazzFieldName(strList.get(0)), getJdbcType(strList.get(1))));
				}
				
				// 在 CREATE TABLE 语句下面找建主键的语句
				if(find && primaryKeyMatcher.matches()) {
					String primaryKeys = primaryKeyMatcher.group(1);
					String[] primaryKeyArray = primaryKeys.split(",");
					List<String> primaryKeyList = new ArrayList<String>();
					for(String primaryKey : primaryKeyArray) {
						primaryKeyList.add(primaryKey.trim());
					}
					
					for(MapperData mapperData : mapperDataList) {
						if(primaryKeyList.contains(mapperData.getSqlName())) {
							mapperData.setPrimaryKey(true);
						}
					}
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
			
			System.out.println("文件名：" + CreateTableSqlToJavaClass.getClazzName(tableName) + 
					"Mapper.xml" + SEPARATOR);

			// 输出 resultMap 节点内容
			System.out.println(createResultMap(tableName, mapperDataList));
			// 输出insert
			System.out.println(createInsert(tableName, mapperDataList));
			// 输出update
			System.out.println(createUpdate(tableName, mapperDataList));
			// 输出delete
			System.out.println(createDelete(tableName, mapperDataList));
			// 输出普通select
			System.out.println(createSelect(tableName, mapperDataList));
			// 输出分页select
			System.out.println(createSelectByPage(tableName, mapperDataList));
			// 输出排序+分页select
			System.out.println(createSelectByOrderAndPage(tableName, mapperDataList));
			
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
	 * 获取JDBC数据类型
	 * 
	 * @param type
	 * @return
	 */
	public static String getJdbcType(String type) {
		if (StringUtils.isBlank(type)) {
			return "";
		}

		type = type.toUpperCase();

		if (type.contains("NUMBER")) {
			return "NUMERIC";

		} else if (type.contains("CHAR")) {
			return "VARCHAR";

		} else if (type.contains("DATE")) {
			return "DATE";

		} else if (type.contains("TIMESTAMP")) {
			return "TIMESTAMP";
			
		// 这里可以添加你用到的类型转换
		} else {
			System.out.println("getJdbcType 未知类型：" + type);
		}

		return "UNDEFINE";
	}
	
	/**
	 * 生成Mapper xml 文件中的 resultMap 部分
	 * @param tableName
	 * @param fieldMap
	 * @return
	 */
	private static String createResultMap(String tableName, List<MapperData> mapperDataList) {
		StringBuilder resultMap = new StringBuilder();
		String className = CreateTableSqlToJavaClass.getClazzName(tableName);
		resultMap.append(String.format("\t<resultMap id=\"%s\" type=\"com.sfpay.**.%s\">" + SEPARATOR, 
				new Object[]{className + "Map", className}));
		for(MapperData mapperData : mapperDataList) {
			resultMap.append(String.format("\t\t<%s column=\"%s\" jdbcType=\"%s\" property=\"%s\" />" + SEPARATOR, 
					new Object[]{mapperData.isPrimaryKey() ? "id" : "result", mapperData.getSqlName(), mapperData.getJdbcType(), 
							mapperData.getFieldName()}));
		}
		resultMap.append("\t</resultMap>" + SEPARATOR);
		
		return resultMap.toString();
	}
	
	/**
	 * 生成Mapper xml 文件中的 insert 部分
	 * @param tableName
	 * @param fieldMap
	 * @return
	 */
	private static String createInsert(String tableName, List<MapperData> mapperDataList) {
		StringBuilder insert = new StringBuilder();
		String className = CreateTableSqlToJavaClass.getClazzName(tableName);
		insert.append(String.format("\t<insert id=\"addExpressOrder\" parameterType=\"com.sfpay.**.%s\">" + SEPARATOR, 
				new Object[]{className + "Map", className}));
		insert.append("\t\tinsert into " + tableName + SEPARATOR);
		insert.append("\t\t\t(");
		int charNum = 0;	// 记录字符数，用于排版换行
		for(MapperData mapperData: mapperDataList) {
			insert.append(mapperData.getSqlName() + ", ");
			charNum += 2 + mapperData.getSqlName().length();
			// 大约每90个字符一行
			if(charNum >= CHAR_SEPARATOR_NUM && mapperDataList.indexOf(mapperData) != mapperDataList.size() - 1) {
				insert.append(SEPARATOR + "\t\t\t");
				charNum = 0;
			}
		}
		// 除去多余的", "
		insert.replace(insert.length() - 2, insert.length(), "");
		insert.append(")" + SEPARATOR);

		insert.append("\t\tvalues(" + SEPARATOR);
		// ID序列
		insert.append("\t\t\t<selectKey keyProperty=\"id\" resultType=\"long\" order=\"BEFORE\">"
						+ SEPARATOR);
		insert.append(String.format(
				"				select SEQ_%s.Nextval as id from DUAL" + SEPARATOR,
				tableName));
		insert.append("\t\t\t</selectKey>" + SEPARATOR);
		insert.append("\t\t\t");
		charNum = 0;
		for(MapperData mapperData: mapperDataList) {
			insert.append(String.format("#{%s,jdbcType=%s}, ", 
					new Object[]{mapperData.getFieldName(), mapperData.getJdbcType()}));
			
			// 大约每90个字符一行
			charNum += 15 + mapperData.getFieldName().length() + mapperData.getJdbcType().length();
			if(charNum >= CHAR_SEPARATOR_NUM && mapperDataList.indexOf(mapperData) != mapperDataList.size() - 1) {
				insert.append(SEPARATOR + "\t\t\t");
				charNum = 0;
			}
		}
		// 除去多余的", "
		insert.replace(insert.length() - 2, insert.length(), "");
		insert.append(SEPARATOR);
		
		insert.append("\t\t)" + SEPARATOR);
		
		insert.append("\t</insert>" + SEPARATOR);
		
		return insert.toString();
	}
	
	/**
	 * 生成Mapper xml 文件中的 update 部分
	 * @param tableName
	 * @param fieldMap
	 * @return
	 */
	private static String createUpdate(String tableName, List<MapperData> mapperDataList) {
		StringBuilder update = new StringBuilder();
		String className = CreateTableSqlToJavaClass.getClazzName(tableName);
		update.append(String.format("\t<update id=\"%s\">" + SEPARATOR, "update" + className));
		update.append(String.format("\t\tupdate %s" + SEPARATOR, tableName));
		update.append("\t\t<set>" + SEPARATOR);
		for(MapperData mapperData: mapperDataList) {
			// 非主键才能Set
			if(!mapperData.isPrimaryKey()) {
				if(mapperData.getJdbcType().contains("CHAR")) {
					update.append(String.format("\t\t\t<if test=\"%s != null and %s != ''\">%s = #{%s,jdbcType=%s},</if>" + SEPARATOR,
							new Object[] { mapperData.getFieldName(), mapperData.getFieldName(), mapperData.getSqlName(),
							mapperData.getFieldName(), mapperData.getJdbcType()}));
					
				} else {
					update.append(String.format("\t\t\t<if test=\"%s != null\">%s = #{%s,jdbcType=%s},</if>" + SEPARATOR,
							new Object[] { mapperData.getFieldName(), mapperData.getSqlName(),
							mapperData.getFieldName(), mapperData.getJdbcType()}));
				}
			}
		}
		update.append("\t\t</set>" + SEPARATOR);
		update.append("\t\twhere ");
		for(MapperData mapperData: mapperDataList) {
			if(mapperData.isPrimaryKey()) {
				update.append(String.format("%s = #{%s} and ", new Object[] {mapperData.getSqlName(),
						mapperData.getFieldName() }));
			}
		}
		// 除去多余的" and "
		if(update.toString().endsWith(" and ")) {
			update.replace(update.length() - 5, update.length(), "");
		} else if(update.toString().endsWith("where ")) {
			update.replace(update.length() - 6, update.length(), "");
		}
		update.append(SEPARATOR);
		update.append("\t</update>" + SEPARATOR);
		
		return update.toString();
	}
	
	/**
	 * 生成Mapper xml 文件中的 delete 部分
	 * @param tableName
	 * @param fieldMap
	 * @return
	 */
	private static String createDelete(String tableName, List<MapperData> mapperDataList) {
		StringBuilder delete = new StringBuilder();
		String className = CreateTableSqlToJavaClass.getClazzName(tableName);
		delete.append(String.format("\t<delete id=\"%s\">" + SEPARATOR, "del" + className));
		delete.append(String.format("\t\tdelete from %s where ", tableName));
		for(MapperData mapperData: mapperDataList) {
			if(mapperData.isPrimaryKey()) {
				delete.append(String.format("%s = #{%s} and ", new Object[]{mapperData.getSqlName(), mapperData.getFieldName()}));
			}
		}
		// 除去多余的" and "
		delete.replace(delete.length() - 5, delete.length(), "");
		delete.append(SEPARATOR);
		delete.append("\t</delete>" + SEPARATOR);
		
		return delete.toString();
	}
	
	/**
	 * 生成Mapper xml 文件中的 普通select 部分（非分页）
	 * @param tableName
	 * @param fieldMap
	 * @return
	 */
	private static String createSelect(String tableName, List<MapperData> mapperDataList) {
		StringBuilder select = new StringBuilder();
		String className = CreateTableSqlToJavaClass.getClazzName(tableName);
		select.append(String.format("\t<select id=\"%s\" resultMap=\"%s\" parameterType=\"%s\">" + SEPARATOR, 
				new Object[]{"query" + className + "ByParam", className + "Map", "com.sfpay.**." + className}));
		select.append("\t\tselect ");
		int charNum = 0;	// 记录字符数，用于排版换行
		for(MapperData mapperData: mapperDataList) {
			select.append(mapperData.getSqlName() + ", ");
			// 大约每90个字符一行
			charNum += 2 + mapperData.getSqlName().length();
			if(charNum >= CHAR_SEPARATOR_NUM && mapperDataList.indexOf(mapperData) != mapperDataList.size() - 1) {
				select.append(SEPARATOR + "\t\t\t");
				charNum = 0;
			}
		}
		// 除去多余的", "
		select.replace(select.length() - 2, select.length(), "");
		select.append(SEPARATOR);
		select.append(String.format("\t\tfrom %s" + SEPARATOR, tableName));
		// 构建where子句
		select.append("\t\t<where>" + SEPARATOR);
		for(MapperData mapperData: mapperDataList) {
			if(!mapperData.isPrimaryKey()) {
				if(mapperData.getJdbcType().contains("CHAR")) {
					select.append(String.format("\t\t\t<if test=\"%s != null and %s != ''\">and %s = #{%s}</if>" + SEPARATOR,
							new Object[] { mapperData.getFieldName(), mapperData.getFieldName(), mapperData.getSqlName(),
							mapperData.getFieldName()}));
					
				} else {
					select.append(String.format("\t\t\t<if test=\"%s != null\">and %s = #{%s}</if>" + SEPARATOR,
							new Object[] { mapperData.getFieldName(), mapperData.getSqlName(),
							mapperData.getFieldName()}));
				}
			}
		}
		select.append("\t\t</where>" + SEPARATOR);
		select.append("\t</select>" + SEPARATOR);
		
		return select.toString();
	}
	
	/**
	 * 生成Mapper xml 文件中的分页 select 部分（不排序）
	 * @param tableName
	 * @param fieldMap
	 * @return
	 */
	private static String createSelectByPage(String tableName, List<MapperData> mapperDataList) {
		StringBuilder select = new StringBuilder();
		select.append("	<!-- 不排序直接分页 -->" + SEPARATOR);
		String className = CreateTableSqlToJavaClass.getClazzName(tableName);
		select.append(String.format("\t<select id=\"%s\" resultMap=\"%s\" parameterType=\"%s\">" + SEPARATOR, 
				new Object[]{"query" + className + "ByPage", className + "Map", "com.sfpay.**." + className}));
		int charNum = 0;	// 记录字符数，用于排版换行
		
		//-------- 最外层的select start----------
		charNum = 0;
		select.append("\t\tselect ");
		for(MapperData mapperData: mapperDataList) {
			select.append(mapperData.getSqlName() + ", ");
			// 大约每90个字符一行
			charNum += 2 + mapperData.getSqlName().length();
			if(charNum >= CHAR_SEPARATOR_NUM && mapperDataList.indexOf(mapperData) != mapperDataList.size() - 1) {
				select.append(SEPARATOR + "\t\t\t");
				charNum = 0;
			}
		}
		// 除去多余的", "
		select.replace(select.length() - 2, select.length(), "");
		select.append(SEPARATOR);
		select.append("\t\tfrom" + SEPARATOR);
		//-------- 中间层的select start----------
		charNum = 0;
		select.append("\t\t\t(\tselect ");
		for(MapperData mapperData: mapperDataList) {
			select.append(mapperData.getSqlName() + ", ");
			// 大约每90个字符一行
			charNum += 2 + mapperData.getSqlName().length();
			if(charNum >= CHAR_SEPARATOR_NUM - 10 && mapperDataList.indexOf(mapperData) != mapperDataList.size() - 1) {
				select.append(SEPARATOR + "\t\t\t\t\t");
				charNum = 0;
			}
		}
		select.append("rownum r" + SEPARATOR);
		select.append(String.format("\t\t\t\tfrom %s" + SEPARATOR, tableName));
		select.append("\t\t\t\twhere rownum &lt;= #{end}" + SEPARATOR);
		select.append("\t\t\t)" + SEPARATOR);
		//-------- 中间层的select end----------
		select.append("\t\twhere r &gt;= #{start}" + SEPARATOR);		
		
		select.append("\t</select>" + SEPARATOR);
		
		return select.toString();
	}
	
	/**
	 * 生成Mapper xml 文件中的 select 部分（排序+分页）
	 * @param tableName
	 * @param fieldMap
	 * @return
	 */
	private static String createSelectByOrderAndPage(String tableName, List<MapperData> mapperDataList) {
		StringBuilder select = new StringBuilder();
		select.append("	<!-- 排序+分页 -->" + SEPARATOR);
		String className = CreateTableSqlToJavaClass.getClazzName(tableName);
		select.append(String.format("\t<select id=\"%s\" resultMap=\"%s\" parameterType=\"%s\">" + SEPARATOR, 
				new Object[]{"query" + className + "ByOrderPage", className + "Map", "com.sfpay.**." + className}));
		int charNum = 0;	// 记录字符数，用于排版换行
		
		//-------- 最外层的select start----------
		charNum = 0;
		select.append("\t\tselect ");
		for(MapperData mapperData: mapperDataList) {
			select.append(mapperData.getSqlName() + ", ");
			// 大约每90个字符一行
			charNum += 2 + mapperData.getSqlName().length();
			if(charNum >= CHAR_SEPARATOR_NUM && mapperDataList.indexOf(mapperData) != mapperDataList.size() - 1) {
				select.append(SEPARATOR + "\t\t\t");
				charNum = 0;
			}
		}
		// 除去多余的", "
		select.replace(select.length() - 2, select.length(), "");
		select.append(SEPARATOR);
		select.append("\t\tfrom" + SEPARATOR);
		//-------- 中间层的select start----------
		charNum = 0;
		select.append("\t\t\t(\tselect ");
		for(MapperData mapperData: mapperDataList) {
			select.append(mapperData.getSqlName() + ", ");
			// 大约每90个字符一行
			charNum += 2 + mapperData.getSqlName().length();
			if(charNum >= CHAR_SEPARATOR_NUM - 10 && mapperDataList.indexOf(mapperData) != mapperDataList.size() - 1) {
				select.append(SEPARATOR + "\t\t\t\t\t");
				charNum = 0;
			}
		}
		select.append("rownum r" + SEPARATOR);
		select.append("\t\t\t\tfrom" + SEPARATOR);
		//-------- 最里层的排过序的select start----------
		select.append("\t\t\t\t\t(\tselect ");
		for(MapperData mapperData: mapperDataList) {
			select.append(mapperData.getSqlName() + ", ");
			// 大约每90个字符一行
			charNum += 2 + mapperData.getSqlName().length();
			if(charNum >= CHAR_SEPARATOR_NUM - 20 && mapperDataList.indexOf(mapperData) != mapperDataList.size() - 1) {
				select.append(SEPARATOR + "\t\t\t\t\t\t\t");
				charNum = 0;
			}
		}
		// 除去多余的", "
		select.replace(select.length() - 2, select.length(), "");
		select.append(SEPARATOR);
		select.append(String.format("\t\t\t\t\t\tfrom %s" + SEPARATOR, tableName));
		// 默认用UPDATE_TIME字段排序，你需要修改
		select.append(String.format("\t\t\t\t\t\torder by %s %s" + SEPARATOR, new Object[]{"UPDATE_TIME",
				"<!-- 排序字段，需修改 -->"}));
		select.append("\t\t\t\t\t) t1" + SEPARATOR);
		//-------- 最里层的排过序的select end----------
		select.append("\t\t\t\twhere rownum &lt;= #{end}" + SEPARATOR);
		select.append("\t\t\t)" + SEPARATOR);
		//-------- 中间层的select end----------
		select.append("\t\twhere r &gt;= #{start}" + SEPARATOR);		
		
		select.append("\t</select>" + SEPARATOR);
		
		return select.toString();
	}
}
