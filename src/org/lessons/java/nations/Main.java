package org.lessons.java.nations;

import java.sql.*;
import java.util.Scanner;

public class Main {
    private final static String URL = System.getenv("DB_URL");
    private final static String USER = System.getenv("DB_USER");
    private final static String PASSWORD = System.getenv("DB_PASSWORD");

    public static void main(String[] args) {
        Scanner scan = new Scanner(System.in);
        System.out.println("Inserisci un filtro di ricerca");
        String stringaUtente = scan.nextLine();

        try(Connection con = DriverManager.getConnection(URL,USER,PASSWORD)){
            String query = """
                    SELECT c.name as nome_nazione, c.country_id as id_nazione , r.name as nome_regione,c2.name as nome_continente \s
                    FROM countries c\s
                    JOIN regions r ON c.region_id = r.region_id\s
                    JOIN continents c2 ON c2.continent_id = r.continent_id\s
                    WHERE c.name  LIKE ?
                    ORDER BY c.name\s
                    """;

            try(PreparedStatement ps = con.prepareStatement(query, ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_READ_ONLY)){

                ps.setString(1, "%" + stringaUtente + "%");

                try(ResultSet rs = ps.executeQuery()){
                    if (!rs.next()){
                        System.out.println("Nessuno risultato");
                    } else {
                        rs.beforeFirst();
                        printResults();
                    }
                    while (rs.next()){
                        String nomeNazione = rs.getString("nome_nazione");
                        int idNazione = rs.getInt("id_nazione");
                        String nomeRegione = rs.getString("nome_regione");
                        String nomeContinente = rs.getString("nome_continente");
                        printResults(nomeNazione,idNazione,nomeRegione,nomeContinente);
                    }
                }

            }
        } catch (SQLException e){
            e.printStackTrace();
        }
    }
    public static void printResults(String nomeNazione, int idNazione, String nomeRegione, String nomeContinente){
        System.out.printf("%20s", nomeNazione);
        System.out.printf("%20s", idNazione);
        System.out.printf("%30s", nomeRegione);
        System.out.printf("%30s\n", nomeContinente);
    }
    public static void printResults(){
        System.out.printf("%20s", "Nazione");
        System.out.printf("%20s", "ID");
        System.out.printf("%30s", "Regione");
        System.out.printf("%30s\n", "Continente");
        System.out.println();
    }
}
