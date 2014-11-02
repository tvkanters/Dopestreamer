package com.dopelives.dopestreamer.streams.services;

import com.dopelives.dopestreamer.streams.Quality;

/**
 * The service for Xphome restreams.
 */
public class Restream extends Xphome {

    /** The default Dopelives channel that will be changed based on the quality */
    private static final String DEFAULT_CHANNEL = "default";

    /**
     * {@inheritDoc}
     */
    @Override
    public String getKey() {
        return "restream";
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getLabel() {
        return "Restream";
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getDefaultChannel() {
        return DEFAULT_CHANNEL;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getConnectionDetails(String channel, final Quality quality) {
        if (channel.equals(DEFAULT_CHANNEL)) {
            switch (quality) {
                case BEST:
                    channel = "restream/restream";
                    break;
                case WORST:
                    channel = "restream_low/restream_low";
                    break;

                default:
                    throw new RuntimeException("Invalid quality for Restream: " + quality);
            }
        }

        return super.getConnectionDetails(channel, quality);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isConnectPossible(final String channel) {
        return super.isChannelPossible(channel.equals("default") ? "restream" : channel);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isChannelPossible(final String channel) {
        switch (channel) {
            case "default":
            case "restream/restream":
            case "restream_low/restream_low":
                return true;

            default:
                return false;
        }
    }

}
