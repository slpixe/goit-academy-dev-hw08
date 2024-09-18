package org.example.service;

import com.codahale.metrics.MetricRegistry;
import org.example.db.ConnectionManager;
import org.example.db.SQLExecutor;
import org.example.mapper.ClientMapper;
import org.example.model.Client;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.example.constants.Constants.*;

public class ClientService {
    private static final Logger logger = LoggerFactory.getLogger(ClientService.class);
    private final ConnectionManager connectionManager;
    private final MetricRegistry metricRegistry;
    private final GenericDatabaseService<Client> clientService;

    public ClientService(ConnectionManager connectionManager, MetricRegistry metricRegistry) {
        this.connectionManager = connectionManager;
        this.metricRegistry = metricRegistry;
        this.clientService = new GenericDatabaseService<>(connectionManager, metricRegistry);
    }

    public long create(String name){
        try (Connection connection = connectionManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(INSERT_INTO_CLIENTS, Statement.RETURN_GENERATED_KEYS)) {

            statement.setString(1, name);

            int affectedRows = statement.executeUpdate();

            if (affectedRows > 0) {
                try (ResultSet generatedKeys = statement.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        return generatedKeys.getLong(1);
                    }
                }
            } else {
                logger.error("Failed to insert the client, no rows affected");
            }
        } catch (SQLException e) {
            logger.error("Error adding a client to the database", e);
        }
        return -1;
    }

    public long createClient(Client client) {
        return clientService.insertEntity(INSERT_INTO_CLIENTS, client, new ClientMapper());
    }

    public Optional<Client> addClient(String name) {

        try (Connection connection = connectionManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(INSERT_INTO_CLIENTS, Statement.RETURN_GENERATED_KEYS)) {

            statement.setString(1, name);
            int affectedRows = statement.executeUpdate();

            if (affectedRows > 0) {
                try (ResultSet generatedKeys = statement.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        long generatedId = generatedKeys.getLong(1);
                        Client client = new Client(generatedId, name);
                        return Optional.of(client);
                    }
                }
            } else {
                logger.error("Failed to insert the client, no rows affected.");
            }
        } catch (SQLException e) {
            logger.error("Error adding a client to the database", e);
        }

        return Optional.empty();
    }


    public String getById(long id){
        return "";
    }

    public void setName(long id, String name){
        try (Connection connection = connectionManager.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(SET_NEW_CLIENTS_NAME)) {

            preparedStatement.setString(1, name);
            preparedStatement.setLong(2, id);

            int affectedRows = preparedStatement.executeUpdate();
            if (affectedRows > 0) {
                logger.info("Successfully updated a client with id {}", id);
            } else {
                logger.error("Failed to update a client with id {}", id);
            }
        } catch (SQLException e){
            logger.error("Error updating a client by id", e);
        }
    }

    public void deleteById(long id){
        try (Connection connection = connectionManager.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(DELETE_CLIENT_BY_ID)) {
            preparedStatement.setLong(1, id);

            int affectedRows = preparedStatement.executeUpdate();

            if (affectedRows > 0) {
                logger.info("Client with id {} was deleted", id);
            } else {
                logger.error("Failed to delete the client, no rows affected");
            }
        } catch (SQLException e) {
            logger.error("Error deleting a client from the database", e);
        }
    }

    public List<Client> listAll(){
        List<Client> clients = new ArrayList<>();

        try (Connection connection = connectionManager.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(LIST_ALL_CLIENTS)) {

            ResultSet resultSet = preparedStatement.executeQuery();

            while (resultSet.next()) {
                Client client = new Client();
                client.setId(resultSet.getLong("id"));
                client.setName(resultSet.getString("name"));
                clients.add(client);
            }
        } catch (SQLException e) {
            logger.error("Error fetching clients from a database", e);
        }

        return clients;
    }

    public Optional<List<Client>> listAllClients() {
        String errorMessage = "Failed to execute findMaxSalaryWorker query";

        try (SQLExecutor executor = new SQLExecutor(connectionManager.getConnection(), metricRegistry)) {
            return executor.executeQuery(
                    LIST_ALL_CLIENTS,
                    errorMessage,
                    rs -> new Client(rs.getLong("id"), rs.getString("name"))
            );
        }
    }
}
