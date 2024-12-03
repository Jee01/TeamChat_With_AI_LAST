package me.project.teamchat_with_ai.chat.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.type.descriptor.jdbc.VarcharJdbcType;

import java.math.BigInteger;


@Getter @Setter
@Table(name = "room")
@Entity
public class Room {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "room_id")
    private Long roomId;

    @Column(name = "name", nullable = false, unique = true)
    private String name;

    public Room() {}

    public Room(String name) {
        this.name = name;
    }

    public Long getRoomId() {
        return roomId;
    }

    public void setRoomId(Long roomId) {
        this.roomId = roomId;
    }

    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return "Room [roomId=" + roomId + ", name=" + name + "]";
    }
}
