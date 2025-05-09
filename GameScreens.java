package sldl;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.Ellipse2D;
import java.awt.geom.RoundRectangle2D;
import java.io.File;
import java.io.IOException;

public class GameScreens extends JPanel implements KeyListener, MouseListener, ActionListener {
    
    // 게임 상태
    private enum GameState {
        START, PLAYING, GAME_OVER
    }
    
    private GameState gameState = GameState.START;
    private int score = 0;
    
    // 화면 크기
    private final int WIDTH = 600;  // 가로 600픽셀
    private final int HEIGHT = 800; // 세로 800픽셀
    
    // 재생 버튼 속성
    private Ellipse2D.Double playButton;
    private final int BUTTON_SIZE = 80;
    private boolean mouseOverButton = false;
    
    // 배경 그라데이션 색상
    private Color[] backgroundColors = {
        new Color(20, 30, 48),  // 진한 남색
        new Color(36, 59, 85)   // 미디엄 남색
    };
    
    // 폰트 설정
    private Font customFont;
    private Font defaultFont;
    
    // 파티클 효과를 위한 클래스
    private class Particle {
        float x, y;
        float speedX, speedY;
        float size;
        float alpha;
        Color color;
        
        Particle(float x, float y, Color color) {
            this.x = x;
            this.y = y;
            this.speedX = (float)(Math.random() * 2 - 1);
            this.speedY = (float)(Math.random() * -2 - 1);
            this.size = (float)(Math.random() * 3 + 1);
            this.alpha = 1.0f;
            this.color = color;
        }
        
        void update() {
            x += speedX;
            y += speedY;
            alpha -= 0.01f;
            if (alpha < 0) alpha = 0;
        }
        
        void draw(Graphics2D g) {
            if (alpha <= 0) return;
            
            g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha));
            g.setColor(color);
            g.fillOval((int)x, (int)y, (int)size, (int)size);
            g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1.0f));
        }
    }
    
    // 별 효과를 위한 클래스
    private class Star {
        float x, y;
        float size;
        float alpha;
        float pulse;
        
        Star() {
            this.x = (float)(Math.random() * WIDTH);
            this.y = (float)(Math.random() * HEIGHT);
            this.size = (float)(Math.random() * 2 + 1);
            this.alpha = (float)(Math.random() * 0.5 + 0.5);
            this.pulse = (float)(Math.random() * Math.PI * 2);
        }
        
        void update() {
            pulse += 0.05f;
            if (pulse > Math.PI * 2) pulse = 0;
        }
        
        void draw(Graphics2D g) {
            float currentAlpha = alpha * (0.7f + (float)Math.sin(pulse) * 0.3f);
            g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, currentAlpha));
            g.setColor(Color.WHITE);
            g.fillOval((int)x, (int)y, (int)size, (int)size);
            g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1.0f));
        }
    }
    
    // 파티클 배열
    private Particle[] particles = new Particle[100];
    private Star[] stars = new Star[150];
    
    // 타이머
    private Timer timer;
    private final int DELAY = 10;
    
    // 애니메이션을 위한 변수
    private int animationTick = 0;
    
    public GameScreens() {
        initGame();
    }
    
    private void initGame() {
        addKeyListener(this);
        addMouseListener(this);
        setFocusable(true);
        setPreferredSize(new Dimension(WIDTH, HEIGHT));
        
        // 재생 버튼 초기화
        int buttonX = (WIDTH - BUTTON_SIZE) / 2;
        int buttonY = HEIGHT / 2 + 30;
        playButton = new Ellipse2D.Double(buttonX, buttonY, BUTTON_SIZE, BUTTON_SIZE);
        
        // 폰트 초기화
        initFonts();
        
        // 별 초기화
        for (int i = 0; i < stars.length; i++) {
            stars[i] = new Star();
        }
        
        // 게임 타이머 시작
        timer = new Timer(DELAY, this);
        timer.start();
    }
    
    private void initFonts() {
        try {
            // 메이플스토리 폰트 로드 시도
            File fontFile = new File("./fonts/메이플스토리.ttf");  // 폰트 파일 경로
            
            if (fontFile.exists()) {
                // 폰트 파일이 존재하면 로드
                customFont = Font.createFont(Font.TRUETYPE_FONT, fontFile).deriveFont(20f);
                GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
                ge.registerFont(customFont);
                System.out.println("Maplestory Bold 폰트를 로드했습니다.");
            } else {
                // 폰트 파일이 없으면 기본 폰트 사용
                System.out.println("폰트 파일을 찾을 수 없습니다: " + fontFile.getAbsolutePath());
                customFont = new Font("SansSerif", Font.BOLD, 20);
            }
        } catch (IOException | FontFormatException e) {
            // 폰트 로드 실패 시 기본 폰트 사용
            System.err.println("폰트 로드 실패: " + e.getMessage());
            customFont = new Font("SansSerif", Font.BOLD, 20);
        }
        
        // 기본 폰트 설정 (폰트 로드 실패 시 대체용)
        defaultFont = new Font("SansSerif", Font.PLAIN, 20);
        
        // 콘솔에 현재 사용 가능한 모든 폰트 출력 (디버깅용)
        String[] fontNames = GraphicsEnvironment.getLocalGraphicsEnvironment().getAvailableFontFamilyNames();
        System.out.println("사용 가능한 폰트:");
        for (String fontName : fontNames) {
            System.out.println(fontName);
        }
    }
    
    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        
        // 렌더링 품질 향상
        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        
        // 배경 그라데이션 그리기
        GradientPaint gradient = new GradientPaint(0, 0, backgroundColors[0], 
                                 0, HEIGHT, backgroundColors[1]);
        g2d.setPaint(gradient);
        g2d.fillRect(0, 0, WIDTH, HEIGHT);
        
        // 별 그리기
        for (Star star : stars) {
            star.draw(g2d);
        }
        
        switch (gameState) {
            case START:
                drawStartScreen(g2d);
                break;
            case PLAYING:
                drawPlaceholderGameScreen(g2d);
                break;
            case GAME_OVER:
                drawGameOverScreen(g2d);
                break;
        }
        
        Toolkit.getDefaultToolkit().sync();
    }
    
    private void drawStartScreen(Graphics2D g) {
        // 제목 패널 - 둥근 직사각형
        RoundRectangle2D.Double titlePanel = new RoundRectangle2D.Double(
            WIDTH/2 - 250, HEIGHT/4 - 50, 500, 100, 20, 20);
            
        // 반투명 패널 효과
        g.setColor(new Color(0, 0, 0, 80));
        g.fill(titlePanel);
        
        // 패널 테두리
        g.setColor(new Color(255, 255, 255, 40));
        g.setStroke(new BasicStroke(2));
        g.draw(titlePanel);
        
        // 타이틀 텍스트
        Font titleFont = customFont.deriveFont(Font.BOLD, 48);
        g.setFont(titleFont);
        
        // 그라데이션 텍스트
        GradientPaint textGradient = new GradientPaint(
            WIDTH/2 - 150, HEIGHT/4 - 20, new Color(255, 255, 255),  // 흰색
            WIDTH/2 + 150, HEIGHT/4 + 20, new Color(173, 216, 230),  // 연한 파랑
            true);
        g.setPaint(textGradient);
        
        String title = "블록 깨기 게임";
        FontMetrics titleFm = g.getFontMetrics();
        g.drawString(title, (WIDTH - titleFm.stringWidth(title)) / 2, HEIGHT/4 + 20);
        
        // 재생 버튼 그리기
        drawPlayButton(g);
    }
    
    private void drawPlayButton(Graphics2D g) {
        // 버튼 배경 - 그라데이션 원
        GradientPaint buttonGradient;
        
        if (mouseOverButton) {
            // 마우스 오버 시 밝은 색상
            buttonGradient = new GradientPaint(
                (float)playButton.x, (float)playButton.y, 
                new Color(72, 209, 204), // 터쿼이즈
                (float)(playButton.x + playButton.width), (float)(playButton.y + playButton.height), 
                new Color(0, 128, 128),  // 틸
                true);
        } else {
            // 기본 색상
            buttonGradient = new GradientPaint(
                (float)playButton.x, (float)playButton.y, 
                new Color(70, 130, 180), // 스틸 블루
                (float)(playButton.x + playButton.width), (float)(playButton.y + playButton.height), 
                new Color(25, 25, 112),  // 미드나잇 블루
                true);
        }
        
        g.setPaint(buttonGradient);
        g.fill(playButton);
        
        // 버튼 테두리
        g.setColor(new Color(255, 255, 255, 100));
        g.setStroke(new BasicStroke(2));
        g.draw(playButton);
        
        // 재생 아이콘 그리기 (삼각형)
        g.setColor(Color.WHITE);
        int[] xPoints = {
            (int)(playButton.x + playButton.width * 0.35),
            (int)(playButton.x + playButton.width * 0.35),
            (int)(playButton.x + playButton.width * 0.75)
        };
        int[] yPoints = {
            (int)(playButton.y + playButton.height * 0.25),
            (int)(playButton.y + playButton.height * 0.75),
            (int)(playButton.y + playButton.height * 0.5)
        };
        g.fillPolygon(xPoints, yPoints, 3);
        
        // 버튼 설명 텍스트
        g.setFont(customFont.deriveFont(Font.BOLD, 18));
        String buttonText = "게임 시작";
        FontMetrics fm = g.getFontMetrics();
        int textX = (int)(playButton.x + (playButton.width - fm.stringWidth(buttonText)) / 2);
        int textY = (int)(playButton.y + playButton.height + 30);
        
        // 실제 텍스트
        g.setColor(Color.WHITE);
        g.drawString(buttonText, textX, textY);
        
        // 버튼 빛나는 효과 (마우스 오버 시)
        if (mouseOverButton) {
            // 내부 글로우
            float alpha = 0.5f + (float)Math.sin(animationTick * 0.1) * 0.2f;
            g.setColor(new Color(255, 255, 255, (int)(alpha * 100)));
            g.setStroke(new BasicStroke(2));
            Ellipse2D.Double glow = new Ellipse2D.Double(
                playButton.x + 5, 
                playButton.y + 5, 
                playButton.width - 10, 
                playButton.height - 10
            );
            g.draw(glow);
            
            // 외부 글로우
            g.setColor(new Color(255, 255, 255, 50));
            g.setStroke(new BasicStroke(2, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            Ellipse2D.Double outGlow = new Ellipse2D.Double(
                playButton.x - 3, 
                playButton.y - 3, 
                playButton.width + 6, 
                playButton.height + 6
            );
            g.draw(outGlow);
        }
    }
    
    private void drawPlaceholderGameScreen(Graphics2D g) {
        // 정보 패널 - 둥근 직사각형
        RoundRectangle2D.Double infoPanel = new RoundRectangle2D.Double(
            WIDTH/2 - 200, HEIGHT/2 - 50, 400, 100, 15, 15);
            
        // 반투명 패널 효과
        g.setColor(new Color(0, 0, 0, 100));
        g.fill(infoPanel);
        
        // 패널 테두리
        g.setColor(new Color(255, 255, 255, 60));
        g.setStroke(new BasicStroke(1));
        g.draw(infoPanel);
        
        // 텍스트
        g.setColor(Color.WHITE);
        g.setFont(customFont.deriveFont(Font.PLAIN, 20));
        
        String placeholder = "여기에 게임 화면이 표시됩니다";
        FontMetrics fm = g.getFontMetrics();
        g.drawString(placeholder, (WIDTH - fm.stringWidth(placeholder)) / 2, HEIGHT / 2 - 10);
        
        String escText = "ESC를 눌러 게임 오버 화면으로 이동";
        g.setFont(customFont.deriveFont(Font.PLAIN, 16));
        fm = g.getFontMetrics();
        g.drawString(escText, (WIDTH - fm.stringWidth(escText)) / 2, HEIGHT / 2 + 20);
    }
    
    private void drawGameOverScreen(Graphics2D g) {
        // 배경에 어두운 오버레이 추가
        g.setColor(new Color(0, 0, 0, 150));
        g.fillRect(0, 0, WIDTH, HEIGHT);
        
        // "게임 오버" 헤더 패널
        RoundRectangle2D.Double headerPanel = new RoundRectangle2D.Double(
            WIDTH/2 - 200, HEIGHT/3 - 50, 400, 80, 15, 15);
            
        g.setColor(new Color(139, 0, 0, 200)); // 어두운 빨강
        g.fill(headerPanel);
        
        g.setColor(new Color(255, 69, 0, 150)); // 오렌지 레드
        g.setStroke(new BasicStroke(2));
        g.draw(headerPanel);
        
        // "게임 오버" 텍스트
        Font gameOverFont = customFont.deriveFont(Font.BOLD, 40);
        g.setFont(gameOverFont);
        g.setColor(Color.WHITE);
        
        String gameOver = "게임 오버";
        FontMetrics titleFm = g.getFontMetrics();
        g.drawString(gameOver, (WIDTH - titleFm.stringWidth(gameOver)) / 2, HEIGHT / 3);
        
        // 점수 패널
        RoundRectangle2D.Double scorePanel = new RoundRectangle2D.Double(
            WIDTH/2 - 150, HEIGHT/2 - 30, 300, 60, 15, 15);
            
        g.setColor(new Color(0, 0, 0, 150));
        g.fill(scorePanel);
        
        g.setColor(new Color(255, 215, 0, 100)); // 골드
        g.draw(scorePanel);
        
        // 최종 점수 표시
        Font scoreFont = customFont.deriveFont(Font.BOLD, 30);
        g.setFont(scoreFont);
        g.setColor(new Color(255, 215, 0)); // 골드
        
        String finalScore = "최종 점수: " + score;
        FontMetrics scoreFm = g.getFontMetrics();
        g.drawString(finalScore, (WIDTH - scoreFm.stringWidth(finalScore)) / 2, HEIGHT / 2 + 10);
        
        // 재시작 버튼
        RoundRectangle2D.Double restartButton = new RoundRectangle2D.Double(
            WIDTH/2 - 150, HEIGHT*2/3 - 25, 300, 50, 15, 15);
            
        // 버튼 그라데이션
        GradientPaint buttonGradient = new GradientPaint(
            (float)restartButton.x, (float)restartButton.y, 
            new Color(65, 105, 225), // 로얄 블루
            (float)(restartButton.x + restartButton.width), (float)(restartButton.y + restartButton.height), 
            new Color(25, 25, 112),  // 미드나잇 블루
            true);
        
        g.setPaint(buttonGradient);
        g.fill(restartButton);
        
        g.setColor(new Color(255, 255, 255, 100));
        g.draw(restartButton);
        
        // 재시작 텍스트
        Font restartFont = customFont.deriveFont(Font.BOLD, 20);
        g.setFont(restartFont);
        g.setColor(Color.WHITE);
        
        String restartText = "SPACE 키를 눌러 다시 시작";
        FontMetrics restartFm = g.getFontMetrics();
        g.drawString(restartText, (WIDTH - restartFm.stringWidth(restartText)) / 2, HEIGHT * 2 / 3 + 7);
        
        // 파티클 효과
        for (Particle particle : particles) {
            if (particle != null) {
                particle.draw(g);
            }
        }
    }
    
    private void generateParticles(int count, float x, float y, Color color) {
        for (int i = 0; i < count; i++) {
            int index = (animationTick + i) % particles.length;
            particles[index] = new Particle(x, y, color);
        }
    }
    
    @Override
    public void actionPerformed(ActionEvent e) {
        animationTick++;
        
        // 별 업데이트
        for (Star star : stars) {
            star.update();
        }
        
        // 파티클 업데이트
        for (Particle particle : particles) {
            if (particle != null) {
                particle.update();
            }
        }
        
        // 게임 오버 화면에서 파티클 생성
        if (gameState == GameState.GAME_OVER && animationTick % 10 == 0) {
            float x = (float)(Math.random() * WIDTH);
            float y = HEIGHT - 20;
            
            Color particleColor;
            int colorChoice = (int)(Math.random() * 3);
            if (colorChoice == 0) {
                particleColor = new Color(255, 0, 0);  // 빨강
            } else if (colorChoice == 1) {
                particleColor = new Color(255, 165, 0); // 주황
            } else {
                particleColor = new Color(255, 215, 0); // 노랑
            }
            
            generateParticles(5, x, y, particleColor);
        }
        
        // 마우스 커서 위치 확인
        Point mousePos = getMousePosition();
        if (mousePos != null && gameState == GameState.START) {
            mouseOverButton = playButton.contains(mousePos);
        } else {
            mouseOverButton = false;
        }
        
        repaint();
    }
    
    @Override
    public void mouseClicked(MouseEvent e) {
        if (gameState == GameState.START && playButton.contains(e.getPoint())) {
            gameState = GameState.PLAYING;
        }
    }
    
    @Override
    public void keyPressed(KeyEvent e) {
        int key = e.getKeyCode();
        
        switch (gameState) {
            case START:
                if (key == KeyEvent.VK_SPACE) {
                    gameState = GameState.PLAYING;
                }
                break;
                
            case PLAYING:
                // 게임 화면에서 ESC 키를 누르면 게임 오버 화면으로 이동 (테스트용)
                if (key == KeyEvent.VK_ESCAPE) {
                    gameState = GameState.GAME_OVER;
                }
                break;
                
            case GAME_OVER:
                if (key == KeyEvent.VK_SPACE) {
                    // 게임 재시작 - 시작 화면으로 이동
                    gameState = GameState.START;
                    score = 0;
                    
                    // 파티클 초기화
                    for (int i = 0; i < particles.length; i++) {
                        particles[i] = null;
                    }
                }
                break;
        }
    }
    
    @Override
    public void mousePressed(MouseEvent e) {
        // 사용하지 않음
    }
    
    @Override
    public void mouseReleased(MouseEvent e) {
        // 사용하지 않음
    }
    
    @Override
    public void mouseEntered(MouseEvent e) {
        // 사용하지 않음
    }
    
    @Override
    public void mouseExited(MouseEvent e) {
        // 사용하지 않음
    }
    
    @Override
    public void keyReleased(KeyEvent e) {
        // 사용하지 않음
    }
    
    @Override
    public void keyTyped(KeyEvent e) {
        // 사용하지 않음
    }
    
    public static void main(String[] args) {
        // 폰트 관련 디버깅 옵션 설정
        System.setProperty("awt.useSystemAAFontSettings", "on");
        System.setProperty("swing.aatext", "true");
        
        JFrame frame = new JFrame("블록 깨기 게임");
        GameScreens game = new GameScreens();
        
        frame.add(game);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setResizable(false);
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }
}