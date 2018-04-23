package br.com.caelum.leilao.servico;

import java.util.Calendar;
import java.util.List;

import br.com.caelum.leilao.dominio.IRepositorioDeLeiloes;
import br.com.caelum.leilao.dominio.IRepositorioDePagamentos;
import br.com.caelum.leilao.dominio.Leilao;
import br.com.caelum.leilao.dominio.Pagamento;

public class GeradorDePagamento {

	private final IRepositorioDeLeiloes leiloes;
	private final Avaliador avaliador;
	private final IRepositorioDePagamentos pagamentos;
	
	public GeradorDePagamento(IRepositorioDeLeiloes leiloes, IRepositorioDePagamentos pagamentos, Avaliador avaliador) {
		this.leiloes = leiloes;
		this.avaliador =avaliador;
		this.pagamentos= pagamentos;
	}
	
	public void gera() {
		List<Leilao> leiloesEncerrados = this.leiloes.encerrados();
		for (Leilao leilao : leiloesEncerrados) {
			this.avaliador.avalia(leilao);

			Pagamento novoPagamento = new Pagamento(avaliador.getMaiorLance(), Calendar.getInstance());
			this.pagamentos.salva(novoPagamento);
		}

	}
}
