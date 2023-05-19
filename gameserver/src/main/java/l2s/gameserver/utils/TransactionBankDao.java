package l2s.gameserver.utils;

import l2s.commons.dbutils.DbUtils;
import l2s.gameserver.dao.CharacterDAO;
import l2s.gameserver.database.DatabaseFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class TransactionBankDao {

    private static final Logger _log = LoggerFactory.getLogger(CharacterDAO.class);

    private static TransactionBankDao _instance = new TransactionBankDao();

    public static TransactionBankDao getInstance()
    {
        return _instance;
    }

    public BigDecimal selectExchangeRate(){

        Connection con = null;
        PreparedStatement statement = null;
        BigDecimal rate = BigDecimal.valueOf(0);
        try
        {
            con = DatabaseFactory.getInstance().getConnection();
            statement = con.prepareStatement("SELECT exchange_rate FROM exchange_rate");
            ResultSet resultSet = statement.executeQuery();
            if(resultSet.next()){
                rate = resultSet.getBigDecimal(1);
            }
            DbUtils.close(statement);
        }
        catch(final SQLException e)
        {
            _log.error("", e);
        }
        finally
        {
            DbUtils.closeQuietly(con, statement);
        }
        return rate;
    }

    public void updateExchangeRate(BigDecimal exchangeRate) {
        Connection con = null;
        PreparedStatement statement = null;
        try
        {
            con = DatabaseFactory.getInstance().getConnection();
            statement = con.prepareStatement("UPDATE exchange_rate SET exchange_rate =?");
            statement.setBigDecimal(1,exchangeRate);
            statement.executeUpdate();
            DbUtils.close(statement);
        }
        catch(final SQLException e)
        {
            _log.error("", e);
        }
        finally
        {
            DbUtils.closeQuietly(con, statement);
        }
    }
}
