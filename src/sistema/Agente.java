package sistema;

import ambiente.*;
import arvore.TreeNode;
import arvore.fnComparator;
import problema.*;
import comuns.*;
import static comuns.PontosCardeais.*;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.Random;

/**
 *
 * @author tacla
 */
public class Agente implements PontosCardeais {

    /* referência ao ambiente para poder atuar no mesmo*/
    Model model;
    Problema prob;
    Estado estAtu; // guarda o estado atual (posição atual do agente)
    int plan[];
    double custo;
    static int ct = -1;
    
    String [][] oponentes = new String[9][9];
    int lin_oponente;
    
    String filename="src/Oponentes.txt";
    Path pathToFile = Paths.get(filename);
    
    
    Random random = new Random();
    int empurrar, custo_empurrar;
    double custo_acao, custo_total;
    char oponente_gentil;
   
    

    public Agente(Model m) {
        this.model = m;
        prob = new Problema();
        prob.criarLabirinto(9, 9);
        prob.crencaLabir.porParedeVertical(0, 1, 0);
        prob.crencaLabir.porParedeVertical(0, 0, 1);
        prob.crencaLabir.porParedeVertical(5, 8, 1);
        prob.crencaLabir.porParedeVertical(5, 5, 2);
        prob.crencaLabir.porParedeVertical(8, 8, 2);
        prob.crencaLabir.porParedeHorizontal(4, 7, 0);
        prob.crencaLabir.porParedeHorizontal(7, 7, 1);
        prob.crencaLabir.porParedeHorizontal(3, 5, 2);
        prob.crencaLabir.porParedeHorizontal(3, 5, 3);
        prob.crencaLabir.porParedeHorizontal(7, 7, 3);
        prob.crencaLabir.porParedeVertical(6, 7, 4);
        prob.crencaLabir.porParedeVertical(5, 6, 5);
        prob.crencaLabir.porParedeVertical(5, 7, 7);
        porOponentes();

        // Estado inicial, objetivo e atual
        //posiciona fisiscamente o agente no estado inicial
        Estado ini = this.sensorPosicao();
        prob.defEstIni(ini.getLin(), ini.getCol());

        // Estado atual doa agente = estado inicial
        this.estAtu = prob.estIni;

        // Define o estado objetivo
        prob.defEstObj(1, 8);
        
       

    }
    
    public void porOponentes(){
        
        
        for (int lin = 0; lin < prob.maxLin; lin++){
            for (int col = 0; col < prob.maxCol; col++){
                if (prob.crencaLabir.parede[lin][col] != 1)
                {
                    try {
                        oponentes[lin][col] = (String) Files.readAllLines(pathToFile).get(lin_oponente);
                        lin_oponente ++;
                    } catch (IOException ex) {
                        Logger.getLogger(Agente.class.getName()).log(Level.SEVERE, null, ex);
                    }
                    
                    
                }
            }
        }
    }

    public void printPlano() {
        System.out.println("--- PLANO ---");
        for (int i = 0; i < plan.length; i++) {
            System.out.print(acao[plan[i]] + ">");
        }
        System.out.println("FIM\n\n");
    }

    /**
     * Escolhe qual ação será executada em um ciclo de raciocínio. Na 1a chamada
     * calcula um plano por meio de um algoritmo de busca. A partir da 2a
     * chamada, executa uma ação por vez do plano calculado.
     */
    public int deliberar() {
        // realiza busca na 1a. chamada para elaborar um plano
        if (ct == -1) {
            plan = buscaCheapestFirst(0); //0=c.unif.; 1=A* com colunas; 2=A*=dist. Euclidiana
            if (plan != null) 
                printPlano();
            else {
                System.out.println("SOLUÇÃO NÃO ENCONTRADA");
                return -1;
            }

        }

        // nas demais chamadas, executa o plano já calculado
        ct++;

        // atingiu o estado objetivo então para
        if (prob.testeObjetivo(estAtu)) {
            
            System.out.println("!!! ATINGIU ESTADO OBJETIVO !!!");
            return -1;
        }
        //algo deu errado, chegou ao final do plano sem atingir o objetivo
        if (ct >= plan.length) {
            System.out.println("### ERRO: plano chegou ao fim, mas objetivo não foi atingido");
            return -1;
        }
        
        
        
        // empurrar = 0 -> nao empurra
        // empurrar = 1 -> empurra
        oponente_gentil = oponentes[estAtu.getLin()][estAtu.getCol()].charAt((oponentes[estAtu.getLin()][estAtu.getCol()].length()-1));
    
        empurrar = random.nextInt(2);
        if (empurrar == 0){
            if (oponente_gentil == 'S'){
                custo_empurrar = 1;
            }
            else{
                custo_empurrar = 6;
            }
        }
        else {
            if (oponente_gentil == 'S'){
                custo_empurrar = 4;
            }
            else{
                custo_empurrar = 3;
            }
        }
        
        
        if (acao[plan[ct]] == "N" || acao[plan[ct]] == "S" ||acao[plan[ct]] == "L" ||acao[plan[ct]] == "O" ){
            custo_acao = 1;
        }
        else{
            custo_acao = 1.5;
        }
            
        custo_total = custo_total + custo_acao*custo_empurrar;
        
        
        
        
        System.out.println("--- Mente do Agente ---");
        System.out.println("  Estado atual  : " + estAtu.getLin() + "," + estAtu.getCol());
        System.out.println("  Passo do plano: " + (ct + 1) + " de " + plan.length + " ação=" + acao[plan[ct]]);
        System.out.println("  Oponente: " + oponentes[estAtu.getLin()][estAtu.getCol()]);
        System.out.println("  Empurrar:" + empurrar + "  Custo:" + custo_empurrar);
        System.out.println("  Custo Acumulado:" + custo_total+ "\n");
        executarIr(plan[ct]);
        // atualiza o estado atual baseando-se apenas nas suas crenças e
        // na função sucessora (não faz leitura do sensor de posição!!!)
        estAtu = prob.suc(estAtu, plan[ct]);
        return 1;
    }

    /**
     * Atuador: solicita ao agente 'fisico' executar a acao.
     *
     * @param direcao
     * @return 1 caso movimentacao tenha sido executada corretamente
     */
    public int executarIr(int direcao) {
        model.ir(direcao);
        return 1; // deu certo
    }

    /**
     * Simula um sensor que realiza a leitura da posição atual no ambiente e
     * traduz para um par de coordenadas armazenadas em uma instância da classe
     * Estado.
     *
     * @return Estado um objeto que representa a posição atual do agente no
     * labirinto
     */
    private Estado sensorPosicao() {
        int pos[];
        pos = model.lerPos();
        return new Estado(pos[0], pos[1]);
    }

    public void printExplorados(ArrayList<Estado> expl) {
        System.out.println("--- Explorados --- (TAM: " + expl.size() + ")");
        for (Estado e : expl) {
            System.out.print(e.getString() + " ");
        }
        System.out.println("\n");
    }

    public void printFronteira(ArrayList<TreeNode> front) {
        System.out.println("--- Fronteira --- (TAM=" + front.size() + ")");
        for (TreeNode f : front) {
            String str;
            str = String.format("<%s %.2f+%.2f=%.2f> ", f.getState().getString(),
                    f.getGn(), f.getHn(), f.getFn());
            System.out.print(str);
        }
        System.out.println("\n");
    }

    public int[] montarPlano(TreeNode nSol) {
        int d = nSol.getDepth();
        int sol[] = new int[d];
        TreeNode pai = nSol;

        for (int i = sol.length - 1; i >= 0; i--) {
            sol[i] = pai.getAction();
            pai = pai.getParent();
        }
        return sol;
    }

    /**
     * Implementa uma heurística - a número 1 - para a estratégia A* No caso,
     * hn1 é a distância em colunas do estado passado como argumento até o
     * estado objetivo.
     *
     * @param estado: estado para o qual se quer calcular o valor de hn
     */
    private float hn1(Estado est) {
        return (float) Math.abs(est.getCol() - prob.estObj.getCol());
    }

    /**
     * Implementa uma heurística - a número 2 - para a estratégia A* No caso,
     * hn2 é a distância Euclidiana do estado passado como argumento até o
     * estado objetivo (calculada por Pitágoras).
     *
     * @param estado: estado para o qual se quer calcular o valor de hn
     */
    private float hn2(Estado est) {
        double distCol = Math.abs(est.getCol() - prob.estObj.getCol());
        double distLin = Math.abs(est.getLin() - prob.estObj.getLin());
        return (float) Math.sqrt(Math.pow(distLin, 2) + Math.pow(distCol, 2));
    }

    /**
     * Realiza busca com a estratégia de custo uniforme ou A* conforme escolha
     * realizada na chamada.
     *
     * @param tipo 0=custo uniforme; 1=A* com heurística hn1; 2=A* com hn2
     * @return
     */
   
    public int[] buscaCheapestFirst(int tipo) {
        // atributos para analise de depenho
        int ctNosArvore = 0; // contador de nos gerados e incluidos na arvore
        // nós que foram inseridos na arvore mas que
        // que não necessitariam porque o estado já
        // foi explorado ou por já estarem na fronteira 
        int ctNosDesprFront = 0;
        int ctNosDesprExpl = 0;

        // Algoritmo de busca
        TreeNode sol = null;     // armazena o nó objetivo
        TreeNode raiz = new TreeNode(null);
        raiz.setState(prob.estIni);
        raiz.setGnHn(0, 0);
        raiz.setAction(-1); // nenhuma acao
        ctNosArvore++;

        // cria FRONTEIRA com estado inicial 
        ArrayList<TreeNode> fronteira = new ArrayList<>(12);
        fronteira.add(raiz);

        // cria EXPLORADOS - lista de estados inicialmente vazia
        ArrayList<Estado> expl = new ArrayList<>(12);

        // estado na inicializacao da arvore de busca
        System.out.println("\n*****\n***** INICIALIZACAO ARVORE DE BUSCA\n*****\n");
        System.out.println("\nNós na árvore..............: " + ctNosArvore);
        System.out.println("Desprezados já na fronteira: " + ctNosDesprFront);
        System.out.println("Desprezados já explorados..: " + ctNosDesprExpl);
        System.out.println("Total de nós gerados.......: " + (ctNosArvore + ctNosDesprFront + ctNosDesprExpl));

        while (!fronteira.isEmpty()) {
            System.out.println("\n*****\n***** Inicio iteracao\n*****\n");
            printFronteira(fronteira);
            TreeNode nSel = fronteira.remove(0);
            System.out.println("   Selec. exp.: \n" + nSel.gerarStr() + "\n");

            // teste de objetivo
            if (nSel.getState().igualAo(this.prob.estObj)) {
                sol = nSel;
                //System.out.println("!!! Solução encontrada !!!");
                break;
            }
            expl.add(nSel.getState()); // adiciona estado aos já explorados
            printExplorados(expl);

            // obtem acoes possiveis para o estado selecionado para expansão
            int acoes[] = prob.acoesPossiveis(nSel.getState());
            // adiciona um filho para cada acao possivel
            for (int ac = 0; ac < acoes.length; ac++) {
                if (acoes[ac] < 0) // a acao não é possível
                {
                    continue;
                }
                // INSERE NÓ FILHO NA ÁRVORE DE BUSCA - SEMPRE INSERE, DEPOIS
                // VERIFICA SE O INCLUI NA FRONTEIRA OU NÃO
                // instancia o filho ligando-o ao nó selecionado (nSel)
                TreeNode filho = nSel.addChild();
                // Obtem estado sucessor pela execução da ação <ac>
                Estado estSuc = prob.suc(nSel.getState(), ac);
                filho.setState(estSuc);
                // custo gn: custo acumulado da raiz ate o nó filho
                float gnFilho;
                gnFilho = nSel.getGn() + prob.obterCustoAcao(nSel.getState(), ac, estSuc);

                switch (tipo) {
                    case 0: // busca custo uniforme
                        filho.setGnHn(gnFilho, (float) 0); // deixa hn zerada porque é busca de custo uniforme  
                        break;
                    case 1: // A* com heurística 1
                        filho.setGnHn(gnFilho, hn1(estSuc));
                        break;
                    case 2: // A* com heurística 2
                        filho.setGnHn(gnFilho, hn2(estSuc));
                        break;
                }

                filho.setAction(ac);

                // INSERE NÓ FILHO NA FRONTEIRA (SE SATISFAZ CONDIÇÕES)
                // Testa se estado do nó filho foi explorado
                boolean jaExplorado = false;
                for (Estado e : expl) {
                    if (filho.getState().igualAo(e)) {
                        jaExplorado = true;
                        break;
                    }
                }
                // Testa se estado do nó filho está na fronteira, caso esteja
                // guarda o nó existente em nFront
                TreeNode nFront = null;
                if (!jaExplorado) {
                    for (TreeNode n : fronteira) {
                        if (filho.getState().igualAo(n.getState())) {
                            nFront = n;
                            break;
                        }
                    }
                }

                // se ainda não foi explorado ...
                if (!jaExplorado) {
                    // e não está na fronteira, então adiciona à fronteira
                    if (nFront == null) {
                        fronteira.add(filho);
                        fronteira.sort(new fnComparator()); // classifica ascendente
                        ctNosArvore++;
                    } else {
                        // se jah estah na fronteira temos que ver se eh melhor 
                        if (nFront.getFn() > filho.getFn()) { // no da fronteira tem custo maior que o filho
                            fronteira.remove(nFront);  // remove no da fronteira: pior
                            nFront.remove(); // retira-se da arvore
                            fronteira.add(filho);      // adiciona o filho que eh melhor
                            fronteira.sort(new fnComparator()); // classifica ascendente
                            // nao soma na arvore porque inclui o melhor e retira o pior
                        } else {
                            // conta como desprezado seja porque o filho eh pior e foi descartado
                            ctNosDesprFront++;

                        }
                    }
                } else {
                    ctNosDesprExpl++;
                }
                // esta contagem de maximos perdeu o sentido porque todos os 
                // nos sao armazenados na arvore de busca. Logo, ultima iteracao
                // contem o maximo de nos na arvore (inclusive com a fronteira
                // e os ja explorados (que tambem estao na arvore)
                /*
                if (fronteira.size() > maxNosFronteira)
                    maxNosFronteira = fronteira.size();
                if (expl.size() > maxNosExplorados)
                    maxNosExplorados = expl.size();
                 */
            }
            //raiz.printSubTree();
            System.out.println("\nNós na árvore..............: " + ctNosArvore);
            System.out.println("Desprezados já na fronteira: " + ctNosDesprFront);
            System.out.println("Desprezados já explorados..: " + ctNosDesprExpl);
            System.out.println("Total de nós gerados.......: " + (ctNosArvore + ctNosDesprFront + ctNosDesprExpl));
            //System.out.println("Nós desprezados total..........: " + (ctNosDesprFront + ctNosDesprExpl));
            //System.out.println("Máx nós front..: " + maxNosFronteira);
            //System.out.println("Máx nós explor.: " + maxNosExplorados);
        }

        // classifica a fronteira por 
        //Collections.sort(fronteira, new fnComparator());
        if (sol != null) {
            System.out.println("!!! Solucao encontrada !!!");
            System.out.println("!!! Custo: " + sol.getGn());
            System.out.println("!!! Depth: " + sol.getDepth() + "\n");
            System.out.println("\nNós na árvore..............: " + ctNosArvore);
            System.out.println("Desprezados já na fronteira: " + ctNosDesprFront);
            System.out.println("Desprezados já explorados..: " + ctNosDesprExpl);
            System.out.println("Total de nós gerados.......: " + (ctNosArvore + ctNosDesprFront + ctNosDesprExpl));
            return montarPlano(sol);
        } else {
            System.out.println("### solucao NAO encontrada ###");
            return null;
        }
    }
}
