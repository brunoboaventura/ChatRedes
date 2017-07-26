/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package chatredes;

import java.io.Serializable;

/**
 *
 * @author bruno
 */
public class Sala implements Serializable {

    public String nome;
    public String ip_multicast;

    Sala(String nome, String ip) {
        this.nome = nome;
        this.ip_multicast = ip;
    }
}
