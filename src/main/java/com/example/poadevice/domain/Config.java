package com.example.poadevice.domain;

public class Config {

    private final int id;
    private final String destinationNetworkId;
    private final String transferable;
    private final String metadata;

//Add two more parameters clientid and poa for OAuth implementation
    public Config(
        final Integer id,
        final String destinationNetworkId,
        final String transferable,
        final String metadata
    ) {
        this.id = id;
        this.destinationNetworkId = destinationNetworkId;
        this.transferable = transferable;
        this.metadata = metadata;
    }

    public int getId() {
        return id;
    }

    public String getDestinationNetworkId() {
        return destinationNetworkId;
    }

    public String getTransferable() {
        return transferable;
    }

    public String getMetadata() {
        return metadata;
    }

}
