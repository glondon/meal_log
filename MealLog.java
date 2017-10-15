import java.sql.*;

public class MealLog
{

	private Connection db;
	private Statement stmt;
	private ResultSet rs;

	public MealLog()
	{
		try
		{
			Class.forName("com.mysql.jdbc.Driver");
			db = DriverManager.getConnection("jdbc:mysql://localhost:3306/meal_log", "root", "");
			stmt = db.createStatement();
		}
		catch(Exception ex)
		{
			System.out.println("DB Error: " + ex);
		}
			
	}

	private void getWhys()
	{
		String query = "SELECT why FROM whys ORDER BY why DESC";
		System.out.println("Reasons to lose weight\n");
			
		try
		{
			rs = stmt.executeQuery(query);
			while(rs.next())
				System.out.println(rs.getString(1));

			rs.close();

		}  
		catch(Exception ex)
		{
			System.out.println("DB Error: " + ex);
		}
			


	}

	public static void main(String[] args)
	{
		MealLog m = new MealLog();
		m.getWhys();
		System.exit(0);
	}
}