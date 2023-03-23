package org.lessons.java.nations;

import java.sql.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
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

//            BONUS
            System.out.println();
            System.out.println("Scegli un ID per vedere le stats del paese:");
            String idscelto = scan.nextLine();

            String langQuery = """
                    SELECT c.name, l.`language`\s
                    FROM countries c\s
                    JOIN country_languages cl ON c.country_id = cl.country_id\s
                    JOIN languages l on l.language_id = cl.language_id\s
                    WHERE cl.country_id = ?;
                    """;
            try(PreparedStatement ps = con.prepareStatement(langQuery,ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_READ_ONLY)) {
                ps.setString(1, idscelto);
                try(ResultSet rs = ps.executeQuery()){
                    if (!rs.next()){
                        System.out.println("Nessuno risultato");
                    } else {
                        rs.beforeFirst();
                    }
                    String countryName = null;
                    List<String> languages = new ArrayList<>();
                    while (rs.next()){
                        countryName = rs.getString(1);
                        languages.add(rs.getString(2));
                    }
                    System.out.println();

                    System.out.println(countryName != null ? "Hai selezionate il paese: " + countryName : "");
                    System.out.print(languages.size() > 0 ? "Lingue parlate: " + languages : "");
                    System.out.println();
                }
            }

            String statsQuery = """
                    SELECT c.name, cs.year, cs.population, cs.gdp
                    FROM countries c
                    JOIN country_stats cs ON c.country_id = cs.country_id
                    WHERE c.country_id = ?
                    ORDER BY cs.year DESC
                    LIMIT 1;
                    """;
            try(PreparedStatement ps = con.prepareStatement(statsQuery,ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_READ_ONLY)) {
                ps.setString(1, idscelto);
                try(ResultSet rs = ps.executeQuery()){
                    String year = null;
                    int population = 0;
                    String gdp = null;
                    while (rs.next()){
                        year = rs.getString("year");
                        population = rs.getInt("population");
                        gdp = rs.getString("gdp");
                    }
                    if (year != null) {
                        System.out.println("Statistiche più recenti");
                        System.out.println("Year: " + year);
                        System.out.println("Population: " + population);
                        System.out.println("GDP: " + gdp);
                    }

                }
            }
        } catch (SQLException e){
            e.printStackTrace();
        }
        scan.close();
    }
//    METODI per print
    public static void printResults(String nomeNazione, int idNazione, String nomeRegione, String nomeContinente){
        System.out.printf("%45s", nomeNazione);
        System.out.printf("%45s", idNazione);
        System.out.printf("%55s", nomeRegione);
        System.out.printf("%45s\n", nomeContinente);
    }
    public static void printResults(){
        System.out.printf("%45s", "Nazione");
        System.out.printf("%45s", "ID");
        System.out.printf("%55s", "Regione");
        System.out.printf("%45s\n", "Continente");
        System.out.println();
    }
}
