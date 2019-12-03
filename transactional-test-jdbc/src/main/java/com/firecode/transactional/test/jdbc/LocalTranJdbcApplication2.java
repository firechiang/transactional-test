package com.firecode.transactional.test.jdbc;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.sql.*;

public class LocalTranJdbcApplication2 {
	
    private static final Logger LOG = LoggerFactory.getLogger(LocalTranJdbcApplication2.class);

    public static void main(String[] args) throws SQLException {

        String sql = "SELECT * FROM T_USER";
        //String sql = "SELECT * FROM T_USER FOR UPDATE"; 锁全表查询（注意：FOR UPDATE可以锁单条记录，在查询语句后面加一个wher id = xxx就是可以了）
        String plusAmountSQL = "UPDATE T_USER SET amount = ? WHERE username = ?";

        Connection dbConnection = getDBConnection();
        LOG.debug("Begin session2");

        PreparedStatement queryPS = dbConnection.prepareStatement(sql);
        ResultSet rs = queryPS.executeQuery();
        Long superManAmount = 0L;
        while (rs.next()) {
            String name = rs.getString(2);
            Long amount = rs.getLong(3);
            LOG.info("{} has amount:{}", name, amount);
            if (name.equals("SuperMan")) {
                superManAmount = amount;
            }
        }

        PreparedStatement updatePS = dbConnection.prepareStatement(plusAmountSQL);
        updatePS.setLong(1, superManAmount + 100);
        updatePS.setString(2, "SuperMan");
        /**
         * 可先启动LocalTranJdbcApplication并在提交事物的地方打上断点，再启动当前这个类，看看程序是不是会停在这里，
         * 等LocalTranJdbcApplication提交事物后，当前程序才会继续往执行。（测试两个事物修改同一行数据，是不是串行顺序执行的）
         */
        updatePS.executeUpdate();

        LOG.debug("Done session2!");
        queryPS.close();
        updatePS.close();
        dbConnection.close();
    }

    private static Connection getDBConnection() throws SQLException {
        String DB_DRIVER = "com.mysql.jdbc.Driver";
        String DB_CONNECTION = "jdbc:mysql://localhost:3306/dist_tran_course";
        String DB_USER = "mt";
        String DB_PASSWORD = "111111";
        try {
            Class.forName(DB_DRIVER);
        } catch (ClassNotFoundException e) {
            LOG.error(e.getMessage());
        }
        return DriverManager.getConnection(DB_CONNECTION, DB_USER, DB_PASSWORD);
    }

}
