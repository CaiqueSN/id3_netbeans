/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package sistema;

import ambiente.*;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author tacla
 */
public class Main {
    public static void main(String args[]) {
        // Cria o ambiente (modelo) = labirinto com suas paredes
        Model model = new Model(9, 9);
        model.labir.porParedeVertical(0, 1, 0);
        model.labir.porParedeVertical(0, 0, 1);
        model.labir.porParedeVertical(5, 8, 1);
        model.labir.porParedeVertical(5, 5, 2);
        model.labir.porParedeVertical(8, 8, 2);
        model.labir.porParedeHorizontal(4, 7, 0);
        model.labir.porParedeHorizontal(7, 7, 1);
        model.labir.porParedeHorizontal(3, 5, 2);
        model.labir.porParedeHorizontal(3, 5, 3);
        model.labir.porParedeHorizontal(7, 7, 3);
        model.labir.porParedeVertical(6, 7, 4);
        model.labir.porParedeVertical(5, 6, 5);
        model.labir.porParedeVertical(5, 7, 7);
        
        
        FileWriter arq = null;
        int anterior = 0;
        int lin_oponente = 8 + anterior;
        int estrategia = 1; // estrategia = 0 : baseline, estrategia = 1 : J48
        
        if(estrategia == 0){
            try {
                arq = new FileWriter("src/resultadosBaseline.txt");
            } catch (IOException ex) {
                Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        else{
            try {
                arq = new FileWriter("src/resultadosJ48.txt");
            } catch (IOException ex) {
                Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        
        
        PrintWriter gravarArq = new PrintWriter(arq); 
        Agente ag = null;
        double custo_medio = 0; // custo medio depois da execução dos 100 cenarios
        
        
        for (int cenario = 1; cenario <= 100; cenario++){ // Para executar os 100 cenarios pedidos
            // seta a posição inicial do agente no ambiente - corresponde ao estado inicial
            model.setPos(8, 0);


            // Cria um agente
            ag = new Agente(model);
            lin_oponente = ag.porOponentes(lin_oponente);
            
            
                    // marca no ambiente onde estah o objetivo - somente para visualizacao
            model.setObj(ag.prob.estObj.getLin(),ag.prob.estObj.getCol());

            // Ciclo de execucao do sistema
            // desenha labirinto
            model.desenhar(); 

            // agente escolhe proxima açao e a executa no ambiente (modificando
            // o estado do labirinto porque ocupa passa a ocupar nova posicao)

            System.out.println("\n*** Inicio do ciclo de raciocinio do agente ***\n");
            while (ag.deliberar(estrategia) != -1) {  
                model.desenhar(); 
            }
            ag.ct = -1;
            if(estrategia == 0){
                gravarArq.printf("Baseline, Cenario: " + cenario + ", Custo Total:" + ag.custo_total + "\n");
            }
            else{
                gravarArq.printf("J48, Cenario: " + cenario + ", Custo Total:" + ag.custo_total + "\n");
            }
            custo_medio = custo_medio + ag.custo_total;
        }
        
        
        custo_medio = custo_medio/100;
        
        if(estrategia == 0){
            gravarArq.printf("\nBaseline, Custo Medio:" + custo_medio + " Linha final de Oponentes: " + lin_oponente + "\n");
        }
        else{
            gravarArq.printf("\nJ48, Custo Medio:" + custo_medio + "\n");
        }
        
        
        
        try {
            arq.close();
        } catch (IOException ex) {
            Logger.getLogger(Agente.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
