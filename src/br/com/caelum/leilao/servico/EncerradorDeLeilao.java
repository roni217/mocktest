package br.com.caelum.leilao.servico;

import java.util.Calendar;
import java.util.List;

import br.com.caelum.leilao.dominio.IEnviadorDeEmail;
import br.com.caelum.leilao.dominio.IRepositorioDeLeiloes;
import br.com.caelum.leilao.dominio.Leilao;
import br.com.caelum.leilao.infra.dao.LeilaoDao;

public class EncerradorDeLeilao {

	private int total = 0;
	private final IRepositorioDeLeiloes dao;
	private final IEnviadorDeEmail carteiro;
    
    public EncerradorDeLeilao(IRepositorioDeLeiloes dao, IEnviadorDeEmail carteiro) {
        this.dao = dao;
        this.carteiro = carteiro;
    }
    

    public void encerra() {
        List<Leilao> todosLeiloesCorrentes = dao.correntes();

        for (Leilao leilao : todosLeiloesCorrentes) {
            if (comecouSemanaPassada(leilao)) {
                leilao.encerra();
                total++;
                dao.atualiza(leilao);
                carteiro.envia(leilao);
            }
        }
    }

    private boolean comecouSemanaPassada(Leilao leilao) {
        return diasEntre(leilao.getData(), Calendar.getInstance()) >= 7;
    }

    private int diasEntre(Calendar inicio, Calendar fim) {
        Calendar data = (Calendar) inicio.clone();
        int diasNoIntervalo = 0;
        while (data.before(fim)) {
            data.add(Calendar.DAY_OF_MONTH, 1);
            diasNoIntervalo++;
        }
        return diasNoIntervalo;
    }

    public int getTotalEncerrados() {
        return total;
    }
}
