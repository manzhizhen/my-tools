/**
 * 
 */
package com.sfpay.tools.util;

/**
 * 类说明：
 *
 * 类描述：
 * @author manzhizhen
 *
 * 2015年3月15日
 */
/**
 * 类说明：
 *
 * 类描述：
 * @author manzhizhen
 *
 * 2015年3月15日
 */
public class Test {
	public static void main(String[] args) {
		String sql = "CONSTRAINT PK_EWS_INDEX_DEF PRIMARY KEY (WARN_INDEX_NO, WARN_SOURCE, WARN_LEVEL) USING INDEX PCTFREE 10 INITRANS 2 STORAGE ( INITIAL 64K NEXT 1024K MINEXTENTS 1 MAXEXTENTS UNLIMITED ) TABLESPACE EWS_IDX_TS1 LOGGING ) PCTFREE 10 INITRANS 1 STORAGE ( INITIAL 64K NEXT 1024K MINEXTENTS 1 MAXEXTENTS UNLIMITED ) TABLESPACE EWS_DAT_TS1 LOGGING MONITORING NOPARALLEL";
		
		System.out.println(sql.matches("\\s*CONSTRAINT\\s+\\S+\\s+PRIMARY\\s+KEY[^\\(]*\\(([^\\)]+)\\).*"));
	}
}
